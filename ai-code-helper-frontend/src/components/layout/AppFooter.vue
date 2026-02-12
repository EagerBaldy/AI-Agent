<template>
  <footer class="home-footer">
    <div class="footer-content">
      <div class="footer-column brand-column">
        <div class="footer-logo">AI Agent</div>
        <p class="footer-desc">您的智能工作与生活伙伴，赋能无限可能。</p>
        <p class="copyright">© 2026 AI Code Helper. All rights reserved.</p>
      </div>
      
      <div class="footer-column">
        <h4>产品</h4>
        <a href="#">功能介绍</a>
        <a href="#">使用指南</a>
        <a href="#">更新日志</a>
      </div>
      
      <div class="footer-column">
        <h4>资源</h4>
        <a href="#">开发者文档</a>
        <a href="#">社区论坛</a>
        <a href="#">常见问题</a>
      </div>
      
      <div class="footer-column">
        <h4>联系我们</h4>
        <div class="social-links">
          <a href="#" class="social-link" title="GitHub">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path></svg>
          </a>
          <a href="#" class="social-link" title="Twitter">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"></path></svg>
          </a>
          <a href="#" class="social-link" title="Email">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.home-footer {
  width: 100%;
  margin-top: auto;
  padding: 60px 20px 40px;
  background: var(--bg-body);
  border-top: 1px solid var(--border-color);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 40px;
}

.footer-column {
  flex: 1;
  min-width: 200px;
}

.brand-column {
  flex: 1.5;
}

.footer-logo {
  font-size: 1.5rem;
  font-weight: 800;
  background: linear-gradient(45deg, var(--primary-color, #2193b0), #6dd5ed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 16px;
}

.footer-desc {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 24px;
  max-width: 300px;
}

.copyright {
  color: var(--text-secondary);
  font-size: 0.8rem;
  opacity: 0.8;
}

.footer-column h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.footer-column a {
  display: block;
  color: var(--text-secondary);
  text-decoration: none;
  margin-bottom: 12px;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.footer-column a:hover {
  color: var(--primary-color, #2193b0);
}

.social-links {
  display: flex;
  gap: 16px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.social-link:hover {
  color: var(--primary-color, #2193b0);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .footer-column {
    min-width: 100%;
    text-align: center;
  }
  
  .brand-column {
    align-items: center;
    display: flex;
    flex-direction: column;
  }
  
  .social-links {
    justify-content: center;
  }
}
</style>
