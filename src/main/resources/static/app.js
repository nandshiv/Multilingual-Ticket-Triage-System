document.addEventListener('DOMContentLoaded', () => {
    // --- Theme Logic ---
    const themeToggleBtn = document.getElementById('theme-toggle');
    const root = document.documentElement;
    if (localStorage.getItem('theme') === 'dark') {
        root.setAttribute('data-theme', 'dark');
        themeToggleBtn.innerHTML = '☀️ Light Mode';
    }
    themeToggleBtn.addEventListener('click', () => {
        if (root.getAttribute('data-theme') === 'dark') {
            root.removeAttribute('data-theme');
            localStorage.setItem('theme', 'light');
            themeToggleBtn.innerHTML = '🌙 Dark Mode';
        } else {
            root.setAttribute('data-theme', 'dark');
            localStorage.setItem('theme', 'dark');
            themeToggleBtn.innerHTML = '☀️ Light Mode';
        }
    });

    // --- Navigation Logic ---
    const navLinks = document.querySelectorAll('.nav-links li');
    const views = document.querySelectorAll('.queue-container');
    const filterBox = document.getElementById('filter-box');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            navLinks.forEach(l => l.classList.remove('active'));
            e.target.classList.add('active');
            const targetView = e.target.getAttribute('data-view');
            
            views.forEach(v => v.classList.add('hidden'));
            document.getElementById(targetView).classList.remove('hidden');
            
            filterBox.classList.toggle('hidden', targetView !== 'view-inbox');

            if (targetView === 'view-inbox') loadTickets();
            if (targetView === 'view-clusters') loadClusters();
            if (targetView === 'view-analytics') loadAnalytics();
        });
    });

    // --- Ticket Detail Panel Logic ---
    const detailPanel = document.getElementById('detail-panel');
    let currentTicketId = null;
    document.getElementById('close-panel').addEventListener('click', () => detailPanel.classList.remove('open'));

    document.getElementById('btn-resolve').addEventListener('click', async () => {
        if (!currentTicketId) return;
        try {
            await fetch(`/api/tickets/${currentTicketId}/status?status=RESOLVED`, { method: 'PATCH' });
            detailPanel.classList.remove('open');
            loadTickets();
        } catch (e) { alert("Error resolving ticket"); }
    });

    document.getElementById('btn-reassign').addEventListener('click', async () => {
        if (!currentTicketId) return;
        const teamId = document.getElementById('reassign-team-select').value;
        try {
            await fetch(`/api/tickets/${currentTicketId}/team?teamId=${teamId}`, { method: 'PATCH' });
            detailPanel.classList.remove('open');
            loadTickets();
        } catch (e) { alert("Error reassigning ticket"); }
    });

    // --- Load Data Functions ---
    async function loadTickets() {
        const tbody = document.getElementById('ticket-body');
        const status = document.getElementById('status-filter').value;
        try {
            let url = '/api/tickets';
            if (status !== 'ALL') url += `?status=${status}`;
            const res = await fetch(url);
            if (!res.ok) throw new Error();
            const tickets = await res.json();
            
            tickets.sort((a, b) => (b.priorityScore || 0) - (a.priorityScore || 0));
            tbody.innerHTML = '';
            if (tickets.length === 0) return tbody.innerHTML = `<tr class="empty-state"><td colspan="6">No tickets found.</td></tr>`;
            
            tickets.forEach(ticket => {
                const tr = document.createElement('tr');
                const score = ticket.priorityScore || 0;
                let badgeClass = score >= 40 ? 'score-high' : (score >= 20 ? 'score-med' : 'score-low');
                
                tr.innerHTML = `
                    <td><span class="score-badge ${badgeClass}">${score}</span></td>
                    <td><span class="badge">${ticket.status}</span></td>
                    <td style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                        ${ticket.translatedText || ticket.rawText}
                    </td>
                    <td>${ticket.customer?.name || 'Unknown'}</td>
                    <td>${ticket.category || 'Unclassified'}</td>
                    <td class="text-mute">${new Date(ticket.createdAt).toLocaleTimeString()}</td>
                `;
                tr.addEventListener('click', () => openTicketDetail(ticket));
                tbody.appendChild(tr);
            });
        } catch (e) {
            tbody.innerHTML = `<tr class="empty-state"><td colspan="6" style="color:red;">Error fetching API. Make sure backend is running.</td></tr>`;
        }
    }

    async function loadTeamsForSelect() {
        try {
            const res = await fetch('/api/teams');
            if (res.ok) {
                const teams = await res.json();
                const select = document.getElementById('reassign-team-select');
                select.innerHTML = '';
                teams.forEach(t => {
                    select.innerHTML += `<option value="${t.id}">${t.name}</option>`;
                });
            }
        } catch (e) { console.warn("Could not load teams"); }
    }

    function openTicketDetail(ticket) {
        currentTicketId = ticket.id;
        document.getElementById('detail-category').innerText = ticket.category || 'Unclassified';
        document.getElementById('detail-confidence').innerText = ticket.routingConfidence ? 
            `${(ticket.routingConfidence * 100).toFixed(0)}%` : 'No ML';
        
        document.getElementById('detail-lang').innerText = `(${ticket.detectedLanguage || 'en'})`;
        document.getElementById('detail-original').innerText = ticket.rawText || 'N/A';
        document.getElementById('detail-translated').innerText = ticket.translatedText || 'N/A';
        document.getElementById('detail-score').innerText = ticket.priorityScore || 0;
        
        const bdBox = document.getElementById('detail-breakdown');
        bdBox.innerHTML = '';
        if (ticket.priorityBreakdown && Object.keys(ticket.priorityBreakdown).length > 0) {
            Object.keys(ticket.priorityBreakdown).forEach(key => {
                bdBox.innerHTML += `
                    <div class="breakdown-item">
                        <span>${key.toUpperCase()}</span>
                        <strong>+${ticket.priorityBreakdown[key].points} pts</strong>
                    </div>`;
            });
        } else {
            bdBox.innerHTML = '<span class="text-mute">No breakdown points</span>';
        }
        detailPanel.classList.add('open');
    }

    async function loadClusters() {
        const tbody = document.getElementById('cluster-body');
        try {
            const res = await fetch('/api/clusters');
            if (!res.ok) throw new Error();
            const clusters = await res.json();
            
            tbody.innerHTML = '';
            if (clusters.length === 0) return tbody.innerHTML = `<tr class="empty-state"><td colspan="5">No active clusters.</td></tr>`;
            
            clusters.forEach(c => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><span class="badge">${c.status}</span></td>
                    <td style="max-width: 300px;">${c.representativeText || 'N/A'}</td>
                    <td><strong>${c.ticketCount}</strong> tickets</td>
                    <td class="text-mute">${new Date(c.createdAt).toLocaleTimeString()}</td>
                    <td><button class="btn-outline" onclick="resolveCluster(${c.id})">Resolve All</button></td>
                `;
                tbody.appendChild(tr);
            });
        } catch (e) {
            tbody.innerHTML = `<tr class="empty-state"><td colspan="5">Error fetching clusters.</td></tr>`;
        }
    }

    window.resolveCluster = async (id) => {
        try {
            await fetch(`/api/clusters/${id}/resolve`, { method: 'POST' });
            loadClusters();
        } catch (e) { alert("Error resolving cluster"); }
    };

    let charts = {};
    async function loadAnalytics() {
        try {
            const [volRes, accRes, langRes] = await Promise.all([
                fetch('/api/analytics/volume'), fetch('/api/analytics/routing-accuracy'), fetch('/api/analytics/language-distribution')
            ]);
            
            if (volRes.ok) {
                const volData = await volRes.json();
                if (charts.vol) charts.vol.destroy();
                charts.vol = new Chart(document.getElementById('volumeChart'), {
                    type: 'bar',
                    data: { labels: volData.map(d => d.category), datasets: [{ label: 'Tickets', data: volData.map(d => d.count), backgroundColor: '#3b82f6' }] }
                });
            }
            if (langRes.ok) {
                const langData = await langRes.json();
                if (charts.lang) charts.lang.destroy();
                charts.lang = new Chart(document.getElementById('languageChart'), {
                    type: 'pie',
                    data: { labels: langData.map(d => d.detected_language), datasets: [{ data: langData.map(d => d.count), backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'] }] }
                });
            }
            if (accRes.ok) {
                const accData = await accRes.json();
                document.getElementById('accuracy-stats').innerHTML = `
                    <p style="margin-bottom:8px">Total Automated Routes: <strong>${accData.total}</strong></p>
                    <p style="margin-bottom:8px">Agent Overrides: <strong>${accData.overrides}</strong></p>
                    <p style="font-size: 1.5rem; color: var(--priority-low-text); margin-top: 16px;">
                        Routing Accuracy: <strong>${accData.accuracy.toFixed(1)}%</strong>
                    </p>
                `;
            }
        } catch (e) { console.warn("Analytics error", e); }
    }

    document.getElementById('refresh-btn').addEventListener('click', loadTickets);
    
    // Init
    loadTeamsForSelect();
    loadTickets();
});
