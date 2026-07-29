import contextlib
from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from deep_translator import GoogleTranslator
from langdetect import detect
import numpy as np

# Global models
model = None
vectorizer = None
classifier = None

@contextlib.asynccontextmanager
async def lifespan(app: FastAPI):
    global model, vectorizer, classifier
    
    # 1. Load actual multilingual Sentence Transformer
    # paraphrase-multilingual-MiniLM-L12-v2 produces 384d embeddings
    model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
    
    # 2. Train a baseline TF-IDF + Logistic Regression classifier on startup
    # In a real scenario, this would load a pre-trained pickle file
    train_texts = [
        "How do I reset my password?",
        "My account is locked",
        "I need to change my billing address",
        "The server is down and I am getting 500 errors",
        "My credit card was declined",
        "How do I upgrade to platinum tier?",
        "My app keeps crashing",
        "I have a complaint about customer service"
    ]
    train_labels = ["Account", "Account", "Billing", "Technical", "Billing", "Account", "Technical", "Complaint"]
    
    vectorizer = TfidfVectorizer()
    X_train = vectorizer.fit_transform(train_texts)
    classifier = LogisticRegression()
    classifier.fit(X_train, train_labels)
    
    yield
    # Cleanup if needed

app = FastAPI(title="Triage ML Service", lifespan=lifespan)

class TextRequest(BaseModel):
    text: str

class ClassificationResponse(BaseModel):
    category: str
    confidence: float

class TranslationResponse(BaseModel):
    translated_text: str
    detected_language: str

class EmbeddingResponse(BaseModel):
    embedding: list[float]

@app.post("/translate", response_model=TranslationResponse)
def translate(request: TextRequest):
    try:
        lang = detect(request.text)
        if lang != 'en':
            translator = GoogleTranslator(source='auto', target='en')
            translated = translator.translate(request.text)
            return TranslationResponse(translated_text=translated, detected_language=lang)
        else:
            return TranslationResponse(translated_text=request.text, detected_language='en')
    except Exception as e:
        # Fallback gracefully
        return TranslationResponse(translated_text=request.text, detected_language="en")

@app.post("/classify", response_model=ClassificationResponse)
def classify(request: TextRequest):
    X = vectorizer.transform([request.text])
    pred = classifier.predict(X)[0]
    probs = classifier.predict_proba(X)[0]
    confidence = float(np.max(probs))
    return ClassificationResponse(category=pred, confidence=confidence)

@app.post("/embed", response_model=EmbeddingResponse)
def embed(request: TextRequest):
    # Generates exact 384-dimensional vector required by pgvector schema
    emb = model.encode(request.text).tolist()
    return EmbeddingResponse(embedding=emb)
