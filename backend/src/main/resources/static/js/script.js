/**
 * College Library Management System - Master Client Script
 * B.Tech Technical Training (TT) Session Project
 * Vanilla JavaScript communicating with Spring Boot REST APIs
 */

const API_BASE = (() => {
  if (window.location.hostname.includes('github.io')) {
    return 'https://college-library-backend-9skk.onrender.com/api';
  }
  if (window.location.protocol.startsWith('http')) {
    return '/api';
  }
  return 'http://localhost:8085/api';
})();

// ================= AUTHENTICATION & USER SESSION =================
function getAuthUser() {
  try {
    const userJson = localStorage.getItem('libraryUser');
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    return null;
  }
}

function setAuthUser(user) {
  localStorage.setItem('libraryUser', JSON.stringify(user));
}

function clearAuthUser() {
  localStorage.removeItem('libraryUser');
}

function escapeHtml(value) {
  const replacements = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  };
  return String(value ?? '').replace(/[&<>"']/g, character => replacements[character]);
}

// Role-based route guard
function checkAuth(allowedRoles = []) {
  const user = getAuthUser();
  if (!user) {
    window.location.href = 'login.html';
    return null;
  }
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    showToast('Unauthorized access. Redirecting...', 'error');
    redirectToDashboard(user.role);
    return null;
  }
  return user;
}

function redirectToDashboard(role) {
  if (role === 'STUDENT') {
    window.location.href = 'student-dashboard.html';
  } else if (role === 'LIBRARIAN') {
    window.location.href = 'librarian-dashboard.html';
  } else if (role === 'ADMIN') {
    window.location.href = 'admin-dashboard.html';
  } else {
    window.location.href = 'index.html';
  }
}

async function logout() {
  try {
    await fetch(`${API_BASE}/logout`, { method: 'POST', credentials: 'include' });
  } catch (err) {
    console.warn('Logout API call failed:', err);
  } finally {
    clearAuthUser();
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
      window.location.href = 'login.html';
    }, 400);
  }
}

// ================= TOAST NOTIFICATION SYSTEM =================
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease-out';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ================= DYNAMIC NAVBAR =================
function initNavbar() {
  const navContainer = document.querySelector('.nav-links');
  if (!navContainer) return;

  const user = getAuthUser();
  let linksHtml = `<li><a href="index.html">Home</a></li><li><a href="books.html">Explore Books</a></li>`;

  if (user) {
    if (user.role === 'STUDENT') {
      linksHtml += `
        <li><a href="student-dashboard.html">Dashboard</a></li>
        <li><a href="my-borrowings.html">My Borrowings</a></li>
      `;
    } else if (user.role === 'LIBRARIAN') {
      linksHtml += `
        <li><a href="librarian-dashboard.html">Dashboard</a></li>
        <li><a href="manage-books.html">Manage Books</a></li>
        <li><a href="issue-return.html">Issue & Return</a></li>
      `;
    } else if (user.role === 'ADMIN') {
      linksHtml += `
        <li><a href="admin-dashboard.html">Admin Dashboard</a></li>
        <li><a href="manage-books.html">Inventory</a></li>
        <li><a href="reports.html">Reports</a></li>
      `;
    }

    linksHtml += `
      <li>
        <div class="user-badge">
          <span>${escapeHtml(user.name)}</span>
          <span class="user-role-tag">${user.role}</span>
        </div>
      </li>
      <li><a href="javascript:void(0)" onclick="logout()" class="btn btn-secondary btn-sm" style="color: #ef4444; border-color: rgba(239,68,68,0.3);">Logout</a></li>
    `;
  } else {
    linksHtml += `
      <li><a href="login.html" class="btn btn-secondary btn-sm">Login</a></li>
      <li><a href="register.html" class="btn btn-primary btn-sm">Register</a></li>
    `;
  }

  navContainer.innerHTML = linksHtml;

  // Highlight active link
  const currentPath = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.nav-links a').forEach(a => {
    if (a.getAttribute('href') === currentPath) {
      a.classList.add('active');
    }
  });
}

// ================= API FETCH HELPERS =================
async function apiGet(endpoint) {
  const res = await fetch(`${API_BASE}${endpoint}`, { credentials: 'include' });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Request failed with status ${res.status}`);
  }
  return res.json();
}

async function apiPost(endpoint, body) {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body)
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || `Request failed with status ${res.status}`);
  }
  return data;
}

async function apiPut(endpoint, body = {}) {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body)
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || `Request failed with status ${res.status}`);
  }
  return data;
}

async function apiDelete(endpoint) {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    method: 'DELETE',
    credentials: 'include'
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || `Request failed with status ${res.status}`);
  }
  return data;
}

// Auto init navbar on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  initNavbar();
});
