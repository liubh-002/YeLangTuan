import React, { useState, useEffect, createContext, useContext } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useParams } from 'react-router-dom';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

const UserContext = createContext(null);

function App() {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [mode, setMode] = useState('PERSONAL');

  useEffect(() => {
    if (token) {
      fetchProfile();
      fetchMode();
    }
  }, [token]);

  async function fetchProfile() {
    try {
      const response = await fetch(`${API_BASE}/api/users/profile`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setUser(data);
      } else {
        logout();
      }
    } catch(e) {
      logout();
    }
  }

  async function fetchMode() {
    try {
      const response = await fetch(`${API_BASE}/api/family/mode`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setMode(data.mode);
      }
    } catch(e) {}
  }

  function login(newToken, userData) {
    setToken(newToken);
    setUser(userData);
    localStorage.setItem('token', newToken);
    fetchMode();
  }

  function logout() {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    setMode('PERSONAL');
  }

  return (
    <UserContext.Provider value={{ user, token, mode, login, logout, setMode, fetchMode }}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/" element={token ? <MainPage /> : <Navigate to="/login" />} />
          <Route path="/profile" element={token ? <ProfilePage /> : <Navigate to="/login" />} />
          <Route path="/family" element={token ? <FamilyMembersPage /> : <Navigate to="/login" />} />
          <Route path="/recipes" element={token ? <RecipeSearchPage /> : <Navigate to="/login" />} />
          <Route path="/recipes/:category" element={token ? <RecipeCategoryPage /> : <Navigate to="/login" />} />
          <Route path="/admin" element={token ? <AdminPage /> : <Navigate to="/login" />} />
                    <Route path="/stock" element={token ? <StockPage /> : <Navigate to="/login" />} />
          <Route path="/bill" element={token ? <BillPage /> : <Navigate to="/login" />} />
          <Route path="/purchase" element={token ? <PurchasePage /> : <Navigate to="/login" />} />
          <Route path="*" element={<Navigate to={token ? "/" : "/login"} />} />
        </Routes>
      </BrowserRouter>
    </UserContext.Provider>
  );
}

function useUser() {
  return useContext(UserContext);
}

function LoginPage() {
  const { login } = useUser();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [gender, setGender] = useState('男');
  const [age, setAge] = useState('');
  const [tastePrefer, setTastePrefer] = useState('');
  const [dietTaboo, setDietTaboo] = useState('无');
  const [monthSalary, setMonthSalary] = useState();
  const [isRegister, setIsRegister] = useState(false);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setMessage('');
    
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      setMessage('手机号必须为11位数字');
      return;
    }
    
    if (isRegister && (!name || !dietTaboo || dietTaboo.trim() === '')) {
      setMessage('姓名和饮食忌口不能为空');
      return;
    }

    setLoading(true);
    try {
      const url = isRegister ? `${API_BASE}/api/auth/register` : `${API_BASE}/api/auth/login`;
      const body = isRegister 
        ? { name, phone, password, gender, age: age ? Number(age) : null, monthSalary: monthSalary ? Number(monthSalary) : null, tastePrefer, dietTaboo }
        : { phone, password };
      
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (response.ok) {
        const data = await response.json();
        login(data.token, data);
        navigate('/');
      } else {
        const error = await response.json();
        setMessage(error.message || '操作失败');
      }
    } catch (error) {
      setMessage('网络连接失败，请检查后端服务');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="brand-row">
          <div className="brand-mark"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="M8 12l2 2 4-4"/></svg></div>
          <div>
            <h1>AI时令膳食预算规划</h1>
            <p>{isRegister ? '注册新账号' : '登录您的账号'}</p>
          </div>
        </div>
        
        {message && <div className="error-message">{message}</div>}
        
        <form onSubmit={handleSubmit} className="login-form">
          {isRegister && (
            <>
              <label>姓名
                <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="请输入姓名" />
              </label>
              <label>手机号
                <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))} 
                       placeholder="请输入11位手机号" />
              </label>
              <label>密码
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="请输入密码" />
              </label>
              <label>年龄
                <input type="number" value={age} onChange={(e) => setAge(e.target.value)} placeholder="请输入年龄" />
              </label>
              <label className="full-width">每月工资(元)
                <input type="number" value={monthSalary} onChange={(e) => setMonthSalary(e.target.value)} placeholder="请输入月薪用于预算规划" />
              </label>
              <label>性别
                <select value={gender} onChange={(e) => setGender(e.target.value)}>
                  <option>男</option>
                  <option>女</option>
                  <option>其他</option>
                </select>
              </label>
              <label className="full-width">口味偏好（多选）
                <div className="tag-group">
                  {['清淡', '香辣', '家常', '高蛋白', '素食'].map(t => (
                    <button type="button" key={t} 
                            className={`tag-btn ${tastePrefer.includes(t) ? 'active' : ''}`}
                            onClick={() => setTastePrefer(tastePrefer.includes(t) 
                              ? tastePrefer.replace(t, '').replace('，', '') 
                              : tastePrefer + (tastePrefer ? '，' : '') + t)}>
                      {t}
                    </button>
                  ))}
                </div>
                <input type="text" value={tastePrefer} onChange={(e) => setTastePrefer(e.target.value)} 
                       placeholder="可自定义补充" className="small-input" />
              </label>
              <label className="full-width">饮食忌口
                <input type="text" value={dietTaboo} onChange={(e) => setDietTaboo(e.target.value)} 
                       placeholder="如：香菜，羊肉（无忌口请填无）" />
              </label>
            </>
          )}
          
          {!isRegister && (
            <>
              <label>手机号
                <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))} 
                       placeholder="请输入11位手机号" />
              </label>
              <label>密码
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="请输入密码" />
              </label>
            </>
          )}

          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? '处理中...' : (isRegister ? '注册' : '登录')}
          </button>
        </form>

        <div className="link-row">
          <span>
            {isRegister ? '已有账号？' : '还没有账号？'}
            <button type="button" className="link-btn" onClick={() => setIsRegister(!isRegister)}>
              {isRegister ? '立即登录' : '立即注册'}
            </button>
          </span>
          {!isRegister && (
            <button type="button" className="link-btn" onClick={() => navigate('/forgot-password')}>
              忘记密码？
            </button>
          )}
        </div>
      </section>
    </main>
  );
}

function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [step, setStep] = useState(1);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function getCode() {
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      setMessage('手机号格式不正确');
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}/api/auth/verify-code`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone })
      });
      if (response.ok) {
        const data = await response.json();
        setMessage('验证码已发送（模拟）：' + data.code);
        setStep(2);
      } else {
        setMessage('获取验证码失败');
      }
    } catch(e) {
      setMessage('网络连接失败');
    } finally {
      setLoading(false);
    }
  }

  async function resetPassword() {
    if (!code || !newPassword) {
      setMessage('请填写验证码和新密码');
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}/api/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, code, newPassword })
      });
      if (response.ok) {
        setMessage('密码重置成功，即将跳转登录页');
        setTimeout(() => navigate('/login'), 2000);
      } else {
        setMessage('重置失败，请检查验证码');
      }
    } catch(e) {
      setMessage('网络连接失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="brand-row">
          <div className="brand-mark"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="M8 12l2 2 4-4"/></svg></div>
          <div>
            <h1>忘记密码</h1>
            <p>通过手机号找回您的账号</p>
          </div>
        </div>
        
        {message && <div className="error-message">{message}</div>}
        
        {step === 1 && (
          <div className="login-form">
            <label>手机号
              <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))} 
                     placeholder="请输入11位手机号" />
            </label>
            <button type="button" className="primary-button" onClick={getCode} disabled={loading}>
              {loading ? '发送中...' : '获取验证码'}
            </button>
          </div>
        )}

        {step === 2 && (
          <div className="login-form">
            <label>验证码
              <input type="text" value={code} onChange={(e) => setCode(e.target.value)} placeholder="请输入6位验证码" />
            </label>
            <label>新密码
              <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="请输入新密码" />
            </label>
            <button type="button" className="primary-button" onClick={resetPassword} disabled={loading}>
              {loading ? '重置中...' : '重置密码'}
            </button>
          </div>
        )}

        <div className="link-row">
          <button type="button" className="link-btn" onClick={() => navigate('/login')}>
            返回登录
          </button>
        </div>
      </section>
    </main>
  );
}

function TopNav({ children, showUserMenu = true }) {
  const { user, logout, mode } = useUser();
  const navigate = useNavigate();
  const [showMenu, setShowMenu] = useState(false);

  return (
    <nav className="top-nav">
      <div className="nav-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="M8 12l2 2 4-4"/></svg>
        <span>AI膳食规划</span>
      </div>
      {children}
      {showUserMenu && user && (
        <div className="nav-user">
          <span className={`mode-badge ${mode.toLowerCase()}`}>{mode === 'FAMILY' ? '家庭模式' : '个人模式'}</span>
          <div className="avatar-menu" onClick={() => setShowMenu(!showMenu)}>
            <div className="avatar">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <span>{user.name}</span>
            {showMenu && (
              <div className="dropdown-menu">
                <button onClick={() => { setShowMenu(false); navigate('/profile'); }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  个人中心
                </button>
                <button onClick={() => { setShowMenu(false); navigate('/family'); }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  家庭管理
                </button>
                <div className="divider"></div>
                <button onClick={() => { logout(); navigate('/login'); }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                  退出登录
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}

function MainPage() {
  const { user, token, mode } = useUser();
  const navigate = useNavigate();
  const [plan, setPlan] = useState(null);
  const [peopleCount, setPeopleCount] = useState(3);
  const [taste, setTaste] = useState('');
  const [weeklyBudget, setWeeklyBudget] = useState(500);
  const [monthlySalary, setMonthlySalary] = useState(user?.monthSalary || 6000);
  const [crowd, setCrowd] = useState('');
  const [dietTaboo, setDietTaboo] = useState('');
  const [favoriteDishes, setFavoriteDishes] = useState('');
  const [breakfastWant, setBreakfastWant] = useState('');
  const [lunchWant, setLunchWant] = useState('');
  const [dinnerWant, setDinnerWant] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [editingDish, setEditingDish] = useState(null);
  const [editDishName, setEditDishName] = useState('');
  const [showDishDetail, setShowDishDetail] = useState(null);

  // Restore plan from sessionStorage on mount
  useEffect(() => {
    const saved = sessionStorage.getItem('mealPlan');
    if (saved) {
      try { setPlan(JSON.parse(saved)); } catch(e) {}
    }
    const savedFilters = sessionStorage.getItem('mealFilters');
    if (savedFilters) {
      try {
        const f = JSON.parse(savedFilters);
        if (f.peopleCount) setPeopleCount(f.peopleCount);
        if (f.weeklyBudget) setWeeklyBudget(f.weeklyBudget);
        if (f.monthlySalary) setMonthlySalary(f.monthlySalary);
        if (f.taste) setTaste(f.taste);
        if (f.dietTaboo) setDietTaboo(f.dietTaboo);
        if (f.favoriteDishes) setFavoriteDishes(f.favoriteDishes);
        if (f.breakfastWant) setBreakfastWant(f.breakfastWant);
        if (f.lunchWant) setLunchWant(f.lunchWant);
        if (f.dinnerWant) setDinnerWant(f.dinnerWant);
        if (f.crowd) setCrowd(f.crowd);
      } catch(e) {}
    }
  }, []);

  // Save plan to sessionStorage whenever it changes
  useEffect(() => {
    if (plan) sessionStorage.setItem('mealPlan', JSON.stringify(plan));
  }, [plan]);

  // Save filters to sessionStorage
  useEffect(() => {
    const filters = { peopleCount, weeklyBudget, monthlySalary, taste, dietTaboo, favoriteDishes, breakfastWant, lunchWant, dinnerWant, crowd };
    sessionStorage.setItem('mealFilters', JSON.stringify(filters));
  }, [peopleCount, weeklyBudget, monthlySalary, taste, dietTaboo, favoriteDishes, breakfastWant, lunchWant, dinnerWant, crowd]);

  async function generatePlan(isSavingMode = false) {
    setLoading(true);
    setMessage('');
    try {
      const response = await fetch(API_BASE + '/api/plans/generate', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          peopleCount: Number(peopleCount),
          taste,
          weeklyBudget: Number(weeklyBudget),
          monthlySalary: Number(monthlySalary || user?.monthSalary || 0),
          crowd,
          avoidIngredients: splitList(dietTaboo),
          favoriteDishes: splitList(favoriteDishes),
          breakfastWant: splitList(breakfastWant),
          lunchWant: splitList(lunchWant),
          dinnerWant: splitList(dinnerWant),
          savingMode: isSavingMode
        })
      });
      if (response.ok) {
        const data = await response.json();
        setPlan(data);
        sessionStorage.setItem('mealPlan', JSON.stringify(data));
      } else {
        const error = await response.json().catch(() => null);
        setMessage(error?.message || '\u751f\u6210\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5');
      }
    } catch (error) {
      setMessage('\u7f51\u7edc\u8fde\u63a5\u5931\u8d25: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function editDish(dayIndex, mealIndex, newName) {
    if (!plan || !newName.trim()) return;
    const updatedPlan = JSON.parse(JSON.stringify(plan));
    updatedPlan.days[dayIndex].meals[mealIndex].dishName = newName;
    setPlan(updatedPlan);
    sessionStorage.setItem('mealPlan', JSON.stringify(updatedPlan));
    setEditingDish(null);
  }

  function handlePrint() { window.print(); }

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875'),
        React.createElement('button', { onClick: () => navigate('/stock'), className: 'nav-link' }, '\u5e93\u5b58'),
        React.createElement('button', { onClick: () => navigate('/bill'), className: 'nav-link' }, '\u8bb0\u8d26'),
        React.createElement('button', { onClick: () => navigate('/purchase'), className: 'nav-link' }, '\u91c7\u8d2d')
      ),
      React.createElement('div', { className: 'nav-user' },
        React.createElement('span', { className: 'mode-badge ' + (mode === 'FAMILY' ? 'family' : 'personal') }, mode === 'FAMILY' ? '\u5bb6\u5ead\u6a21\u5f0f' : '\u4e2a\u4eba\u6a21\u5f0f'),
        React.createElement('div', { className: 'avatar-menu', onClick: () => navigate('/profile') },
          React.createElement('div', { className: 'avatar' }, user?.name ? user.name.charAt(0) : 'U'),
          React.createElement('span', null, user?.name || '\u7528\u6237')
        )
      )
    ),
    React.createElement('div', { className: 'main-layout' },
      React.createElement('aside', { className: 'sidebar' },
        React.createElement('div', { className: 'sidebar-header' },
          React.createElement('h2', null, '\u7b5b\u9009\u6761\u4ef6')
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u57fa\u7840\u4fe1\u606f'),
          React.createElement('div', { className: 'form-row' },
            React.createElement('div', { className: 'form-group', style: { flex: 1 } },
              React.createElement('label', null, '\u5e74\u9f84'),
              React.createElement('input', { className: 'filter-input', type: 'number', value: user?.age || '', disabled: true, placeholder: user?.age || '\u767b\u5f55\u540e\u81ea\u52a8\u586b\u5199' })
            ),
            React.createElement('div', { className: 'form-group', style: { flex: 1 } },
              React.createElement('label', null, '\u6027\u522b'),
              React.createElement('input', { className: 'filter-input', value: user?.gender || '', disabled: true })
            )
          ),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u6bcf\u6708\u5de5\u8d44(\u5143)'),
            React.createElement('input', { className: 'filter-input', type: 'number', value: monthlySalary, onChange: e => setMonthlySalary(Number(e.target.value)), placeholder: '\u6ce8\u518c\u65f6\u586b\u5199\u7684\u5de5\u8d44' })
          )
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u8d22\u52a1\u53c2\u6570'),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u6bcf\u5468\u9884\u7b97(\u5143)'),
            React.createElement('input', { className: 'filter-input', type: 'number', value: weeklyBudget, onChange: e => setWeeklyBudget(Number(e.target.value)) })
          )
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u4eba\u7fa4'),
          React.createElement('select', { className: 'filter-select', value: crowd, onChange: e => setCrowd(e.target.value) },
            React.createElement('option', { value: '' }, '\u8bf7\u9009\u62e9'),
            React.createElement('option', { value: '\u513f\u7ae5' }, '\u513f\u7ae5'),
            React.createElement('option', { value: '\u9752\u5e74' }, '\u9752\u5e74'),
            React.createElement('option', { value: '\u8001\u5e74' }, '\u8001\u5e74'),
            React.createElement('option', { value: '\u75c5\u4eba' }, '\u75c5\u4eba'),
            React.createElement('option', { value: '\u51cf\u80a5' }, '\u51cf\u80a5'),
            React.createElement('option', { value: '\u5065\u8eab' }, '\u5065\u8eab')
          )
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u996e\u98df\u5fcc\u53e3'),
          React.createElement('input', { className: 'filter-input', value: dietTaboo, onChange: e => setDietTaboo(e.target.value), placeholder: '\u591a\u4e2a\u7528\u9017\u53f7\u5206\u9694' })
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u559c\u6b22\u7684\u83dc\u54c1'),
          React.createElement('input', { className: 'filter-input', value: favoriteDishes, onChange: e => setFavoriteDishes(e.target.value), placeholder: '\u591a\u4e2a\u7528\u9017\u53f7\u5206\u9694' })
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u81ea\u5b9a\u4e49\u9700\u6c42'),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u65e9\u9910\u60f3\u5403'),
            React.createElement('input', { className: 'filter-input', value: breakfastWant, onChange: e => setBreakfastWant(e.target.value), placeholder: '\u591a\u4e2a\u7528\u9017\u53f7\u5206\u9694' })
          ),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u5348\u9910\u60f3\u5403'),
            React.createElement('input', { className: 'filter-input', value: lunchWant, onChange: e => setLunchWant(e.target.value), placeholder: '\u591a\u4e2a\u7528\u9017\u53f7\u5206\u9694' })
          ),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u665a\u9910\u60f3\u5403'),
            React.createElement('input', { className: 'filter-input', value: dinnerWant, onChange: e => setDinnerWant(e.target.value), placeholder: '\u591a\u4e2a\u7528\u9017\u53f7\u5206\u9694' })
          )
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u7528\u9910\u4eba\u6570'),
          React.createElement('input', { className: 'filter-input', type: 'number', min: 1, value: peopleCount, onChange: e => setPeopleCount(Number(e.target.value)) })
        ),
        React.createElement('button', { className: 'primary-button', onClick: () => generatePlan(false), disabled: loading, style: { width: '100%', marginTop: '16px' } },
          loading ? '\u6b63\u5728\u751f\u6210...' : 'AI \u4e00\u952e\u751f\u6210\u5468\u4e00\u81f3\u5465\u65e5\u83dc\u8c31'
        ),
        message ? React.createElement('p', { style: { color: '#c2410c', fontSize: '13px', marginTop: '8px', textAlign: 'center' } }, message) : null
      ),
      React.createElement('main', { className: 'main-content' },
        loading ? React.createElement('div', { className: 'loading-overlay' },
          React.createElement('div', { className: 'loading-spinner' },
            React.createElement('div', { className: 'spinner' }),
            React.createElement('p', null, 'AI \u6b63\u5728\u4e3a\u60a8\u751f\u6210\u4e00\u5468\u83dc\u8c31...')
          )
        ) : null,
        !plan ? React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '\u8f93\u5165\u9700\u6c42\u540e\u751f\u6210\u4e00\u5468\u4e09\u9910'),
          React.createElement('p', null, '\u5de6\u4fa7\u9009\u62e9\u53c2\u6570\uff0c\u70b9\u51fb\u201cAI \u4e00\u952e\u751f\u6210\u201d\u5373\u53ef\u83b7\u53d6\u4e2a\u6027\u5316\u83dc\u8c31')
        ) : React.createElement('div', null,
          React.createElement('div', { className: 'content-header' },
            React.createElement('h1', null, 'AI \u5468\u83dc\u8c31'),
            React.createElement('button', { className: 'primary-button small', onClick: handlePrint }, '\u6253\u5370\u83dc\u8c31')
          ),
          plan.warningLevel && plan.warningLevel !== 'NONE' ? React.createElement('div', { className: 'warning-banner ' + (plan.warningLevel === 'HEAVY' ? 'heavy' : 'light') },
            plan.warningMessage || (plan.warningLevel === 'HEAVY' ? '\u8b66\u544a\uff1a\u98df\u6750\u82b1\u9500\u5df2\u8d85\u9884\u7b97' : '\u63d0\u793a\uff1a\u98df\u6750\u82b1\u9500\u63a5\u8fd1\u9884\u7b97\u7ebf')
          ) : null,
          React.createElement('div', { className: 'day-grid' },
            (plan.days || []).map((day, dayIdx) =>
              React.createElement('article', { key: day.day, className: 'day-card' },
                React.createElement('header', null,
                  React.createElement('h3', null, day.day),
                  React.createElement('span', null, '\u00a5' + (day.dailyCost || 0))
                ),
                (day.meals || []).map((meal, mealIdx) =>
                  React.createElement('div', { key: day.day + '-' + meal.mealType, className: 'meal-row' },
                    React.createElement('div', { className: 'meal-top' },
                      React.createElement('strong', null, meal.mealType),
                      React.createElement('span', null, '\u00a5' + (meal.estimatedCost || 0))
                    ),
                    editingDish && editingDish.dayIndex === dayIdx && editingDish.mealIndex === mealIdx ?
                    React.createElement('div', { className: 'dish-edit-inline' },
                      React.createElement('input', { className: 'filter-input', value: editDishName, onChange: e => setEditDishName(e.target.value), style: { flex: 1 } }),
                      React.createElement('button', { className: 'primary-button small', onClick: () => { editDish(dayIdx, mealIdx, editDishName); } }, '\u4fdd\u5b58'),
                      React.createElement('button', { className: 'ghost-button small', onClick: () => setEditingDish(null) }, '\u53d6\u6d88')
                    ) :
                    React.createElement(React.Fragment, null,
                      React.createElement('h4', { style: { cursor: 'pointer' }, onClick: () => setShowDishDetail({ day: day, meal: meal, dayIdx: dayIdx, mealIdx: mealIdx }) }, meal.dishName),
                      React.createElement('p', null, (meal.ingredients || []).join(' / ')),
                      React.createElement('small', null, meal.nutritionNote),
                      React.createElement('div', { className: 'edit-buttons' },
                        React.createElement('button', { className: 'ghost-button small', onClick: () => { setEditingDish({ dayIndex: dayIdx, mealIndex: mealIdx }); setEditDishName(meal.dishName); } }, '\u4fee\u6539'),
                        React.createElement('button', { className: 'ghost-button small', onClick: () => setShowDishDetail({ day: day, meal: meal, dayIdx: dayIdx, mealIdx: mealIdx }) }, '\u8be6\u60c5')
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        showDishDetail ? React.createElement('div', { className: 'modal-overlay', onClick: () => setShowDishDetail(null) },
          React.createElement('div', { className: 'modal-content', onClick: e => e.stopPropagation() },
            React.createElement('h3', null, showDishDetail.meal.dishName),
            React.createElement('p', { style: { color: '#64716b', marginBottom: '12px' } }, showDishDetail.meal.mealType + ' \u00a5' + (showDishDetail.meal.estimatedCost || 0)),
            React.createElement('h4', null, '\u6240\u9700\u98df\u6750'),
            React.createElement('ul', null, (showDishDetail.meal.ingredients || []).map((ing, i) => React.createElement('li', { key: i }, ing))),
            React.createElement('h4', null, '\u70f9\u996a\u505a\u6cd5'),
            React.createElement('p', null, showDishDetail.meal.nutritionNote || '\u6682\u65e0\u8be6\u7ec6\u505a\u6cd5\u8bf4\u660e'),
            React.createElement('div', { className: 'modal-actions' },
              React.createElement('button', { className: 'primary-button', onClick: () => setShowDishDetail(null) }, '\u5173\u95ed')
            )
          )
        ) : null
      )
    )
  );
}

function printMenu() {
    window.print();
  }


function ProfilePage() {
  const { user, token, logout, mode, fetchMode } = useUser();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState('profile');
  const [formData, setFormData] = React.useState({
    name: user?.name || '',
    gender: user?.gender || '',
    age: user?.age || '',
    monthSalary: user?.monthSalary || '',
    tastePrefer: user?.tastePrefer || '',
    dietTaboo: user?.dietTaboo || ''
  });
  const [family, setFamily] = React.useState(null);
  const [familyMembers, setFamilyMembers] = React.useState([]);
  const [joinCode, setJoinCode] = React.useState('');
  const [inviteCode, setInviteCode] = React.useState('');
  const [message, setMessage] = React.useState('');
  const [memberForm, setMemberForm] = React.useState({ name: '', age: '', personTag: '\u666e\u901a', appetite: 3, dietTaboo: '\u65e0' });
  const [showAddMember, setShowAddMember] = React.useState(false);

  React.useEffect(() => {
    fetchProfile();
    fetchFamily();
  }, []);

  async function fetchProfile() {
    try {
      const res = await fetch(API_BASE + '/api/users/profile', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) {
        const data = await res.json();
        setFormData({ name: data.name || '', gender: data.gender || '', age: data.age || '', monthSalary: data.monthSalary || '', tastePrefer: data.tastePrefer || '', dietTaboo: data.dietTaboo || '' });
      }
    } catch(e) {}
  }

  async function fetchFamily() {
    try {
      const res = await fetch(API_BASE + '/api/family/group', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) {
        const data = await res.json();
        setInviteCode(data.inviteCode || '');
        setFamily(data);
      }
    } catch(e) {}
    try {
      const res = await fetch(API_BASE + '/api/family/members', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) setFamilyMembers(await res.json());
    } catch(e) {}
  }

  async function handleSaveProfile(e) {
    e.preventDefault();
    try {
      const res = await fetch(API_BASE + '/api/users/profile', {
        method: 'PUT', headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      setMessage(res.ok ? '\u4fdd\u5b58\u6210\u529f' : '\u4fdd\u5b58\u5931\u8d25');
      if (!res.ok) setTimeout(() => setMessage(''), 2000);
    } catch(e) { setMessage('\u7f51\u7edc\u9519\u8bef'); }
  }

  async function handleCreateFamily() {
    try {
      const res = await fetch(API_BASE + '/api/family/group', { method: 'POST', headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) { const d = await res.json(); setInviteCode(d.inviteCode); fetchFamily(); fetchMode(); }
    } catch(e) { alert('\u521b\u5efa\u5931\u8d25'); }
  }

  async function handleJoinFamily() {
    if (!joinCode.trim()) return;
    try {
      const res = await fetch(API_BASE + '/api/family/join', { method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({ inviteCode: joinCode.trim() }) });
      if (res.ok) { setJoinCode(''); fetchFamily(); fetchMode(); } else { const err = await res.json(); alert(err.message || '\u52a0\u5165\u5931\u8d25'); }
    } catch(e) { alert('\u7f51\u7edc\u9519\u8bef'); }
  }

  async function handleAddMember() {
    if (!memberForm.name || !memberForm.age) return;
    try {
      const res = await fetch(API_BASE + '/api/family/members', { method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify(memberForm) });
      if (res.ok) { setShowAddMember(false); setMemberForm({ name: '', age: '', personTag: '\u666e\u901a', appetite: 3, dietTaboo: '\u65e0' }); fetchFamily(); }
    } catch(e) { alert('\u6dfb\u52a0\u5931\u8d25'); }
  }

  async function handleDeleteMember(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8be5\u6210\u5458\uff1f')) return;
    try { await fetch(API_BASE + '/api/family/members/' + id, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } }); fetchFamily(); } catch(e) {}
  }

  return (
    React.createElement('div', { className: 'app-container' },
      React.createElement(TopNav, null,
        React.createElement('div', { className: 'nav-links' },
          React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875'),
          React.createElement('button', { onClick: () => navigate('/stock'), className: 'nav-link' }, '\u5e93\u5b58'),
          React.createElement('button', { onClick: () => navigate('/bill'), className: 'nav-link' }, '\u8bb0\u8d26')
        ),
        React.createElement('div', { className: 'nav-user' },
          React.createElement('button', { onClick: logout, className: 'logout-btn' }, '\u9000\u51fa\u767b\u5f55')
        )
      ),
      React.createElement('main', { className: 'page-content' },
        React.createElement('div', { className: 'profile-tabs' },
          React.createElement('button', { className: 'profile-tab' + (activeTab === 'profile' ? ' active' : ''), onClick: () => setActiveTab('profile') }, '\u4e2a\u4eba\u8d44\u6599'),
          React.createElement('button', { className: 'profile-tab' + (activeTab === 'family' ? ' active' : ''), onClick: () => setActiveTab('family') }, '\u5bb6\u5ead\u7ba1\u7406')
        ),
        activeTab === 'profile' ?
          React.createElement('div', { className: 'family-section' },
            message ? React.createElement('p', { style: { color: '#1f7a4d', marginBottom: '12px' } }, message) : null,
            React.createElement('div', { className: 'form-grid' },
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u59d3\u540d'),
                React.createElement('input', { className: 'filter-input', value: formData.name, onChange: e => setFormData({...formData, name: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u6027\u522b'),
                React.createElement('select', { className: 'filter-select', value: formData.gender, onChange: e => setFormData({...formData, gender: e.target.value}) },
                  React.createElement('option', { value: '' }, '\u8bf7\u9009\u62e9'),
                  React.createElement('option', { value: '\u7537' }, '\u7537'),
                  React.createElement('option', { value: '\u5973' }, '\u5973')),
              ),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u5e74\u9f84'),
                React.createElement('input', { className: 'filter-input', type: 'number', value: formData.age, onChange: e => setFormData({...formData, age: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u6708\u5ea6\u5de5\u8d44(\u5143)'),
                React.createElement('input', { className: 'filter-input', type: 'number', value: formData.monthSalary, onChange: e => setFormData({...formData, monthSalary: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u996e\u98df\u5fcc\u53e3'),
                React.createElement('input', { className: 'filter-input', value: formData.dietTaboo, onChange: e => setFormData({...formData, dietTaboo: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\u559c\u6b22\u7684\u83dc\u54c1'),
                React.createElement('input', { className: 'filter-input', value: formData.tastePrefer, onChange: e => setFormData({...formData, tastePrefer: e.target.value}) }))
            ),
            React.createElement('button', { className: 'primary-button', onClick: handleSaveProfile, style: { marginTop: '16px', width: '100%' } }, '\u4fdd\u5b58\u4fee\u6539')
          ) :
          React.createElement('div', { className: 'family-section' },
            !inviteCode ?
              React.createElement('div', { style: { textAlign: 'center', padding: '20px' } },
                React.createElement('p', null, '\u60a8\u5c1a\u672a\u521b\u5efa\u5bb6\u5ead\u7ec4'),
                React.createElement('button', { className: 'primary-button', onClick: handleCreateFamily }, '\u521b\u5efa\u5bb6\u5ead')
              ) :
              React.createElement('div', null,
                React.createElement('div', { className: 'invite-code-box' },
                  React.createElement('span', null, '\u9080\u8bf7\u7801:'),
                  React.createElement('span', { className: 'code' }, inviteCode),
                  React.createElement('button', { className: 'ghost-button small', onClick: () => { navigator.clipboard.writeText(inviteCode); alert('\u5df2\u590d\u5236'); } }, '\u590d\u5236')
                ),
                React.createElement('div', { className: 'form-row', style: { marginBottom: '16px' } },
                  React.createElement('input', { className: 'filter-input', value: joinCode, onChange: e => setJoinCode(e.target.value), placeholder: '\u8f93\u5165\u9080\u8bf7\u7801\u52a0\u5165\u5bb6\u5ead', style: { flex: 1 } }),
                  React.createElement('button', { className: 'primary-button', onClick: handleJoinFamily, disabled: !joinCode.trim() }, '\u52a0\u5165')
                ),
                React.createElement('div', { className: 'section-header', style: { marginBottom: '12px' } },
                  React.createElement('h3', { style: { margin: 0 } }, '\u5bb6\u5ead\u6210\u5458 (' + familyMembers.length + ')'),
                  React.createElement('button', { className: 'primary-button small', onClick: () => setShowAddMember(true) }, '\u65b0\u589e\u6210\u5458')
                ),
                showAddMember ? React.createElement('div', { className: 'modal-overlay', onClick: () => setShowAddMember(false) },
                  React.createElement('div', { className: 'modal-content', onClick: e => e.stopPropagation() },
                    React.createElement('h3', null, '\u65b0\u589e\u5bb6\u5ead\u6210\u5458'),
                    React.createElement('div', { className: 'form-group' },
                      React.createElement('label', null, '\u59d3\u540d'),
                      React.createElement('input', { className: 'filter-input', value: memberForm.name, onChange: e => setMemberForm({...memberForm, name: e.target.value}) })),
                    React.createElement('div', { className: 'form-row' },
                      React.createElement('div', { className: 'form-group' },
                        React.createElement('label', null, '\u5e74\u9f84'),
                        React.createElement('input', { className: 'filter-input', type: 'number', value: memberForm.age, onChange: e => setMemberForm({...memberForm, age: e.target.value}) })),
                      React.createElement('div', { className: 'form-group' },
                        React.createElement('label', null, '\u4eba\u7fa4\u6807\u7b7e'),
                        React.createElement('select', { className: 'filter-select', value: memberForm.personTag, onChange: e => setMemberForm({...memberForm, personTag: e.target.value}) },
                          React.createElement('option', { value: '\u666e\u901a' }, '\u666e\u901a'),
                          React.createElement('option', { value: '\u513f\u7ae5' }, '\u513f\u7ae5'),
                          React.createElement('option', { value: '\u9752\u5e74' }, '\u9752\u5e74'),
                          React.createElement('option', { value: '\u8001\u5e74' }, '\u8001\u5e74'),
                          React.createElement('option', { value: '\u75c5\u4eba' }, '\u75c5\u4eba'),
                          React.createElement('option', { value: '\u51cf\u80a5' }, '\u51cf\u80a5'))),
                    ),
                    React.createElement('div', { className: 'form-group' },
                      React.createElement('label', null, '\u996e\u98df\u5fcc\u53e3'),
                      React.createElement('input', { className: 'filter-input', value: memberForm.dietTaboo, onChange: e => setMemberForm({...memberForm, dietTaboo: e.target.value}) })),
                    React.createElement('div', { className: 'modal-actions' },
                      React.createElement('button', { className: 'ghost-button', onClick: () => setShowAddMember(false) }, '\u53d6\u6d88'),
                      React.createElement('button', { className: 'primary-button', onClick: handleAddMember }, '\u786e\u8ba4\u6dfb\u52a0'))
                  )
                ) : null,
                React.createElement('div', { className: 'member-list' },
                  familyMembers.map(m => React.createElement('div', { key: m.id, className: 'member-card' },
                    React.createElement('div', { className: 'member-info' },
                      React.createElement('strong', null, m.name),
                      React.createElement('span', null, m.personTag + ' | ' + (m.dietTaboo !== '\u65e0' ? '\u5fcc\u53e3:' + m.dietTaboo : '\u65e0\u5fcc\u53e3'))
                    ),
                    React.createElement('button', { className: 'ghost-button small danger', onClick: () => handleDeleteMember(m.id) }, '\u5220\u9664')
                  )),
                  familyMembers.length === 0 ? React.createElement('p', { style: { color: '#64716b', textAlign: 'center' } }, '\u6682\u65e0\u5bb6\u5ead\u6210\u5458') : null
                )
              )
          )
      )
    )
  );
}



function FamilyMembersPage() {
  const { token } = useUser();
  const navigate = useNavigate();
  const [members, setMembers] = React.useState([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    async function fetch() {
      try {
        const res = await fetch(API_BASE + '/api/family/members', { headers: { Authorization: 'Bearer ' + token } });
        if (res.ok) setMembers(await res.json());
      } catch(e) {}
      setLoading(false);
    }
    fetch();
  }, []);

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '\u52a0\u8f7d\u4e2d...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875'),
        React.createElement('button', { onClick: () => navigate('/profile'), className: 'nav-link' }, '\u4e2a\u4eba\u4e2d\u5fc3')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, '\u5bb6\u5ead\u6210\u5458'),
      members.length === 0 ?
        React.createElement('div', { className: 'empty-state' }, React.createElement('h2', null, '\u6682\u65e0\u5bb6\u5ead\u6210\u5458')) :
        React.createElement('div', { className: 'member-list' },
          members.map(m => React.createElement('div', { key: m.id, className: 'member-card' },
            React.createElement('div', { className: 'member-info' },
              React.createElement('strong', null, m.name),
              React.createElement('span', null, m.personTag + ' | \u5e74\u9f84:' + m.age)
            )
          ))
        )
    )
  );
}



function RecipeSearchPage() {
  const { token } = useUser();
  const navigate = useNavigate();
  const [keyword, setKeyword] = React.useState('');
  const [recipes, setRecipes] = React.useState([]);
  const [loading, setLoading] = React.useState(false);

  async function search() {
    if (!keyword.trim()) return;
    setLoading(true);
    try {
      const res = await fetch(API_BASE + '/api/recipes/search?keyword=' + encodeURIComponent(keyword.trim()), {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) {
        const text = await res.text();
        try { setRecipes(JSON.parse(text)); } catch(e) { setRecipes([]); }
      }
    } catch(e) {}
    setLoading(false);
  }

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, '\u83dc\u8c31\u641c\u7d22'),
      React.createElement('div', { className: 'search-box' },
        React.createElement('input', { value: keyword, onChange: e => setKeyword(e.target.value), placeholder: '\u8f93\u5165\u98df\u6750\u540d\u79f0\u641c\u7d22\u83dc\u8c31...', onKeyDown: e => { if (e.key === 'Enter') search(); } }),
        React.createElement('button', { className: 'primary-button', onClick: search, disabled: loading }, loading ? '\u641c\u7d22\u4e2d...' : '\u641c\u7d22')
      ),
      recipes.length === 0 && !loading ?
        React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '\u8f93\u5165\u98df\u6750\u540d\u79f0\u641c\u7d22\u83dc\u8c31'),
          React.createElement('p', null, '\u6bd4\u5982\uff1a\u9e21\u86cb\u3001\u7ffb\u8304\u3001\u571f\u8c46...')
        ) :
        React.createElement('div', { className: 'recipe-list' },
          recipes.map((r, idx) => React.createElement('div', { key: idx, className: 'recipe-card' },
            React.createElement('h3', null, r.name),
            React.createElement('div', { className: 'recipe-tags' }, r.category ? React.createElement('span', { className: 'tag' }, r.category) : null),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\u98df\u6750'),
              React.createElement('span', null, (r.ingredients || []).join(' / '))
            ),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\u505a\u6cd5'),
              React.createElement('ol', null, (r.steps || []).map((s, i) => React.createElement('li', { key: i }, s)))
            )
          ))
        )
      )
    )
  ;
}

function RecipeCategoryPage() {
  const { token } = useUser();
  const { category } = useParams();
  const navigate = useNavigate();
  const [recipes, setRecipes] = React.useState([]);

  React.useEffect(() => {
    async function load() {
      try {
        const mockData = { diet: [{ name: '\u51cf\u80a5\u68f1\u89d2\u7092\u9e21\u80f8', category: '\u51cf\u80a5', ingredients: ['\u68f1\u89d2', '\u9e21\u80f8\u8089', '\u5927\u849c'], steps: ['\u68f1\u89d2\u6d17\u51c0\u5207\u6bb5', '\u9e21\u80f8\u5207\u4e1d\u7528\u6599\u9152\u814c\u5236', '\u7092\u9e21\u80f8\u53d8\u8272\u76db\u51fa', '\u7092\u68f1\u89d2\u81f3\u7eff', '\u5012\u56de\u9e21\u80f8\u7ffb\u7092\u8c03\u5473'] }] };
        setRecipes(mockData[category] || []);
      } catch(e) {}
    }
    load();
  }, [category]);

  const categoryNames = { diet: '\u51cf\u80a5\u83dc\u8c31', patient: '\u75c5\u4eba\u83dc\u8c31', teen: '\u9752\u5c11\u5e74\u83dc\u8c31', elder: '\u8001\u5e74\u4eba\u83dc\u8c31' };

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, categoryNames[category] || '\u83dc\u8c31'),
      recipes.length > 0 ?
        React.createElement('div', { className: 'recipe-list' },
          recipes.map((r, idx) => React.createElement('div', { key: idx, className: 'recipe-card' },
            React.createElement('h3', null, r.name),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\u98df\u6750'),
              React.createElement('p', null, (r.ingredients || []).join(', '))
            ),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\u505a\u6cd5'),
              React.createElement('ol', null, (r.steps || []).map((s, i) => React.createElement('li', { key: i }, s)))
            )
          ))
        ) :
        React.createElement('div', { className: 'empty-state' }, React.createElement('h2', null, '\u6682\u65e0\u83dc\u8c31\u6570\u636e'))
    )
  );
}

function AdminPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState('test');

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u8fd4\u56de\u9996\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'admin-tabs' },
        React.createElement('button', { className: 'tab-btn' + (activeTab === 'test' ? ' active' : ''), onClick: () => setActiveTab('test') }, 'AI\u6d4b\u8bd5\u9762\u677f')
      ),
      activeTab === 'test' ?
        React.createElement('div', { className: 'admin-content' },
          React.createElement('h3', null, 'AI\u81ea\u52a8\u5316\u6d4b\u8bd5\u9762\u677f'),
          React.createElement('p', null, '\u81ea\u5b9a\u4e49\u7b5b\u9009\u53c2\u6570\u4e00\u952e\u6267\u884cAI\u751f\u6210\uff0c\u53ef\u89c6\u5316\u8f93\u51faFC/Skill/MCP/Agent\u5b8c\u6574\u8c03\u7528\u65e5\u5fd7'),
          React.createElement('div', { className: 'test-scenarios' },
            React.createElement('div', { className: 'scenario-card' },
              React.createElement('h4', null, '\u591a\u6210\u5458\u6df7\u5408\u5bb6\u5ead\u6d4b\u8bd5'),
              React.createElement('p', null, '\u6d4b\u8bd5Skill4\u591a\u6210\u5458\u6df7\u5408\u996e\u98df\u9002\u914d\u529f\u80fd'),
              React.createElement('button', { className: 'primary-button' }, '\u8fd0\u884c\u6d4b\u8bd5')
            ),
            React.createElement('div', { className: 'scenario-card' },
              React.createElement('h4', null, '\u81ea\u5b9a\u4e49\u83dc\u54c1\u9884\u7b97\u91cd\u7b97'),
              React.createElement('p', null, '\u6d4b\u8bd5Skill5\u81ea\u5b9a\u4e49\u83dc\u54c1\u9884\u7b97\u91cd\u7b97\u529f\u80fd'),
              React.createElement('button', { className: 'primary-button' }, '\u8fd0\u884c\u6d4b\u8bd5')
            )
          )
        ) : null
    )
  );
}

function StockPage() {
  const { token } = useUser();
  const navigate = useNavigate();
  const [stocks, setStocks] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [editingId, setEditingId] = React.useState(null);
  const [editForm, setEditForm] = React.useState({ foodName: '', unit: '\u65a4', stockNum: '' });
  const [showAdd, setShowAdd] = React.useState(false);
  const [addForm, setAddForm] = React.useState({ foodName: '', unit: '\u65a4', stockNum: '' });

  React.useEffect(() => { fetchStocks(); }, []);

  async function fetchStocks() {
    setLoading(true);
    try {
      const res = await fetch(API_BASE + '/api/stock', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) setStocks(await res.json());
    } catch(e) {console.error(e);}
    setLoading(false);
  }

  async function handleAdd() {
    if (!addForm.foodName || !addForm.stockNum) return;
    try {
      await fetch(API_BASE + '/api/stock', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
        body: JSON.stringify(addForm)
      });
      setShowAdd(false);
      setAddForm({ foodName: '', unit: '\u65a4', stockNum: '' });
      fetchStocks();
    } catch(e) {console.error(e);}
  }

  async function handleUpdate(id) {
    try {
      await fetch(API_BASE + '/api/stock/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
        body: JSON.stringify(editForm)
      });
      setEditingId(null);
      fetchStocks();
    } catch(e) {console.error(e);}
  }

  async function handleDelete(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8be5\u98df\u6750\u5e93\u5b58\uff1f')) return;
    try {
      await fetch(API_BASE + '/api/stock/' + id, {
        method: 'DELETE',
        headers: { Authorization: 'Bearer ' + token }
      });
      fetchStocks();
    } catch(e) {console.error(e);}
  }

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '\u52a0\u8f7d\u4e2d...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\u9996\u9875'),
        React.createElement('button', { onClick: () => navigate('/bill'), className: 'nav-link' }, '\u8bb0\u8d26')
      ),
      React.createElement('div', { className: 'nav-user' },
        React.createElement('button', { onClick: () => navigate('/purchase'), className: 'ghost-button' }, '\u91c7\u8d2d\u6e05\u5355')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '\u98df\u6750\u5e93\u5b58\u7ba1\u7406'),
        React.createElement('button', { className: 'primary-button small', onClick: () => { setShowAdd(true); setAddForm({ foodName: '', unit: '\u65a4', stockNum: '' }); } }, '\u65b0\u589e\u98df\u6750')
      ),
      showAdd ? React.createElement('div', { className: 'modal-overlay', onClick: () => setShowAdd(false) },
        React.createElement('div', { className: 'modal-content', onClick: e => e.stopPropagation() },
          React.createElement('h3', null, '\u65b0\u589e\u98df\u6750'),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '\u98df\u6750\u540d\u79f0'),
            React.createElement('input', { className: 'filter-input', value: addForm.foodName, onChange: e => setAddForm({...addForm, foodName: e.target.value}), placeholder: '\u8bf7\u8f93\u5165\u98df\u6750\u540d\u79f0' })
          ),
          React.createElement('div', { className: 'form-row' },
            React.createElement('div', { className: 'form-group' },
              React.createElement('label', null, '\u8ba1\u91cf\u5355\u4f4d'),
              React.createElement('select', { className: 'filter-select', value: addForm.unit, onChange: e => setAddForm({...addForm, unit: e.target.value}) },
                React.createElement('option', { value: '\u65a4' }, '\u65a4'),
                React.createElement('option', { value: '\u4e2a' }, '\u4e2a'),
                React.createElement('option', { value: '\u76d2' }, '\u76d2'),
                React.createElement('option', { value: '\u5757' }, '\u5757'),
                React.createElement('option', { value: 'L' }, 'L')
              )
            ),
            React.createElement('div', { className: 'form-group' },
              React.createElement('label', null, '\u5e93\u5b58\u6570\u91cf'),
              React.createElement('input', { className: 'filter-input', type: 'number', value: addForm.stockNum, onChange: e => setAddForm({...addForm, stockNum: e.target.value}), placeholder: '0' })
            )
          ),
          React.createElement('div', { className: 'modal-actions' },
            React.createElement('button', { className: 'ghost-button', onClick: () => setShowAdd(false) }, '\u53d6\u6d88'),
            React.createElement('button', { className: 'primary-button', onClick: handleAdd }, '\u786e\u8ba4\u65b0\u589e')
          )
        )
      ) : null,
      stocks.length === 0 ?
        React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '\u6682\u65e0\u5e93\u5b58\u6570\u636e'),
          React.createElement('p', null, '\u70b9\u51fb\u201c\u65b0\u589e\u98df\u6750\u201d\u6309\u94ae\u6dfb\u52a0\u98df\u6750\u5e93\u5b58')
        ) :
        React.createElement('div', { className: 'stock-grid' },
          stocks.map(stock =>
            React.createElement('div', { key: stock.id, className: 'stock-card' },
              editingId === stock.id ?
                React.createElement('div', { className: 'stock-edit-form' },
                  React.createElement('input', { className: 'filter-input', value: editForm.foodName, onChange: e => setEditForm({...editForm, foodName: e.target.value}) }),
                  React.createElement('div', { className: 'form-row' },
                    React.createElement('select', { className: 'filter-select', value: editForm.unit, onChange: e => setEditForm({...editForm, unit: e.target.value}) },
                      React.createElement('option', { value: '\u65a4' }, '\u65a4'),
                      React.createElement('option', { value: '\u4e2a' }, '\u4e2a'),
                      React.createElement('option', { value: '\u76d2' }, '\u76d2'),
                      React.createElement('option', { value: '\u5757' }, '\u5757'),
                      React.createElement('option', { value: 'L' }, 'L')
                    ),
                    React.createElement('input', { className: 'filter-input', type: 'number', value: editForm.stockNum, onChange: e => setEditForm({...editForm, stockNum: e.target.value}) })
                  ),
                  React.createElement('div', { className: 'edit-buttons' },
                    React.createElement('button', { className: 'primary-button small', onClick: () => handleUpdate(stock.id) }, '\u4fdd\u5b58'),
                    React.createElement('button', { className: 'ghost-button', onClick: () => setEditingId(null) }, '\u53d6\u6d88')
                  )
                ) :
                React.createElement('div', { className: 'stock-display' },
                  React.createElement('div', { className: 'stock-info' },
                    React.createElement('h4', null, stock.foodName),
                    React.createElement('p', { className: 'stock-unit' }, stock.unit)
                  ),
                  React.createElement('div', { className: 'stock-num' },
                    React.createElement('span', { className: 'stock-value ' + (stock.stockNum <= 0 ? 'out-of-stock' : stock.stockNum < 3 ? 'low-stock' : '') }, String(stock.stockNum))
                  ),
                  React.createElement('div', { className: 'stock-actions' },
                    React.createElement('button', { className: 'ghost-button small', onClick: () => { setEditingId(stock.id); setEditForm({ foodName: stock.foodName, unit: stock.unit, stockNum: String(stock.stockNum) }); } }, '\u4fee\u6539'),
                    React.createElement('button', { className: 'ghost-button small danger', onClick: () => handleDelete(stock.id) }, '\u5220\u9664')
                  )
                )
            )
          )
        )
    )
  );
}


﻿function BillPage() {
  const { token } = useUser();
  const navigate = useNavigate();
  const [bills, setBills] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [showAdd, setShowAdd] = React.useState(false);
  const [billForm, setBillForm] = React.useState({ weekCost: '', weekBudget: '', monthSalary: '' });
  const [viewMode, setViewMode] = React.useState('chart');
  React.useEffect(function() { fetchBills(); }, []);
  async function fetchBills() {
    setLoading(true);
    try {
      const res = await fetch(API_BASE + '/api/bills', { headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) setBills(await res.json());
    } catch(e) {console.error(e);}
    setLoading(false);
  }
  async function handleAddBill() {
    if (!billForm.weekCost || !billForm.weekBudget) return;
    try {
      await fetch(API_BASE + '/api/bills', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
        body: JSON.stringify({ weekCost: billForm.weekCost, weekBudget: billForm.weekBudget, monthSalary: billForm.monthSalary || 0 })
      });
      setShowAdd(false);
      setBillForm({ weekCost: '', weekBudget: '', monthSalary: '' });
      fetchBills();
    } catch(e) {console.error(e);}
  }
  async function handleDeleteBill(id) {
    if (!confirm('确信要删除该记录？')) return;
    try { await fetch(API_BASE + '/api/bills/' + id, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } }); fetchBills(); } catch(e) {console.error(e);}
  }
  const totalCost = bills.reduce(function(s, b) { return s + Number(b.weekCost || 0); }, 0);
  const totalBudget = bills.reduce(function(s, b) { return s + Number(b.weekBudget || 0); }, 0);
  const maxCost = Math.max.apply(null, bills.map(function(b) { return Number(b.weekCost || 0); })) || 1;
  const ms = bills.length > 0 ? Number(bills[0].monthSalary || 0) : 0;
  const foodRatio = ms > 0 ? ((totalCost / ms) * 100).toFixed(1) : '0';

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '加载中...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: function() { navigate('/'); }, className: 'nav-link' }, '首页'),
        React.createElement('button', { onClick: function() { navigate('/stock'); }, className: 'nav-link' }, '库存')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '记账统计'),
        React.createElement('div', { className: 'header-actions' },
          React.createElement('button', { className: 'ghost-button' + (viewMode === 'chart' ? ' active' : ''), onClick: function() { setViewMode('chart'); } }, '图表'),
          React.createElement('button', { className: 'ghost-button' + (viewMode === 'list' ? ' active' : ''), onClick: function() { setViewMode('list'); } }, '明细'),
          React.createElement('button', { className: 'primary-button small', onClick: function() { setShowAdd(true); } }, '录入周花销')
        )
      ),
      showAdd ? React.createElement('div', { className: 'modal-overlay', onClick: function() { setShowAdd(false); } },
        React.createElement('div', { className: 'modal-content', onClick: function(e) { e.stopPropagation(); } },
          React.createElement('h3', null, '录入周花销'),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '本周花销(元)'),
            React.createElement('input', { className: 'filter-input', type: 'number', value: billForm.weekCost, onChange: function(e) { setBillForm({...billForm, weekCost: e.target.value}); } })
          ),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '周预算(元)'),
            React.createElement('input', { className: 'filter-input', type: 'number', value: billForm.weekBudget, onChange: function(e) { setBillForm({...billForm, weekBudget: e.target.value}); } })
          ),
          React.createElement('div', { className: 'form-group' },
            React.createElement('label', null, '月度工资(元)'),
            React.createElement('input', { className: 'filter-input', type: 'number', value: billForm.monthSalary, onChange: function(e) { setBillForm({...billForm, monthSalary: e.target.value}); } })
          ),
          React.createElement('div', { className: 'modal-actions' },
            React.createElement('button', { className: 'ghost-button', onClick: function() { setShowAdd(false); } }, '取消'),
            React.createElement('button', { className: 'primary-button', onClick: handleAddBill }, '确认录入')
          )
        )
      ) : null,
      bills.length === 0 ?
        React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '暂无记账数据'),
          React.createElement('p', null, '点击"录入周花销"开始记账')
        ) :
        (viewMode === 'chart' ?
          React.createElement('div', { className: 'chart-area' },
            React.createElement('div', { className: 'chart-section' },
              React.createElement('h3', null, '周花销柱状图'),
              React.createElement('div', { className: 'bar-chart' },
                bills.slice(0, 12).reverse().map(function(bill) {
                  var h = (Number(bill.weekCost) / maxCost) * 200;
                  var over = Number(bill.weekCost) > Number(bill.weekBudget);
                  var near = Number(bill.weekCost) / Number(bill.weekBudget) >= 0.8;
                  var bg = over ? '#fca5a5' : near ? '#fde68a' : '#4ade80';
                  return React.createElement('div', { key: bill.id, className: 'bar-item', title: bill.weekCycle },
                    React.createElement('div', { className: 'bar-fill', style: { height: Math.max(h, 4) + 'px', background: bg } }),
                    React.createElement('span', { className: 'bar-label' }, (bill.weekCycle || '').slice(-5))
                  );
                })
              )
            ),
            ms > 0 ?
              React.createElement('div', { className: 'chart-section' },
                React.createElement('h3', null, '月度收支占比'),
                React.createElement('div', { className: 'pie-chart-container' },
                  React.createElement('svg', { width: '200', height: '200', viewBox: '0 0 200 200' },
                    React.createElement('circle', { cx: '100', cy: '100', r: '80', fill: '#e5e7eb' }),
                    React.createElement('circle', { cx: '100', cy: '100', r: '80', fill: 'transparent', stroke: '#4ade80', strokeWidth: '40',
                      strokeDasharray: String(2*Math.PI*80*Number(foodRatio)/100) + ' ' + String(2*Math.PI*80*(1-Number(foodRatio)/100)),
                      strokeDashoffset: String(2*Math.PI*80*0.25), transform: 'rotate(-90 100 100)' })
                  ),
                  React.createElement('div', { className: 'pie-labels' },
                    React.createElement('div', { className: 'pie-label' },
                      React.createElement('span', { className: 'dot', style: { background: '#4ade80' } }), '膳食 ' + foodRatio + '%'
                    ),
                    React.createElement('div', { className: 'pie-label' },
                      React.createElement('span', { className: 'dot', style: { background: '#e5e7eb' } }), '剩余 ' + String(100 - Number(foodRatio)).substring(0, 5) + '%'
                    )
                  )
                )
              ) : null
          ) :
          React.createElement('div', { className: 'bill-table' },
            React.createElement('table', null,
              React.createElement('thead', null,
                React.createElement('tr', null,
                  React.createElement('th', null, '周期'),
                  React.createElement('th', null, '周预算'),
                  React.createElement('th', null, '周花销'),
                  React.createElement('th', null, '状态'),
                  React.createElement('th', null, '操作')
                )
              ),
              React.createElement('tbody', null,
                bills.map(function(bill) {
                  return React.createElement('tr', { key: bill.id, className: bill.overFlag ? 'over-budget' : '' },
                    React.createElement('td', null, bill.weekCycle),
                    React.createElement('td', null, '¥' + bill.weekBudget),
                    React.createElement('td', null, '¥' + bill.weekCost),
                    React.createElement('td', null, React.createElement('span', { className: 'status-badge ' + (bill.overFlag ? 'danger' : 'success') }, bill.overFlag ? '超支' : '正常')),
                    React.createElement('td', null, React.createElement('button', { className: 'ghost-button small danger', onClick: function() { handleDeleteBill(bill.id); } }, '删除'))
                  );
                })
              )
            )
          )
        )
    )
  );
}


﻿function PurchasePage() {
  const { token } = useUser();
  const navigate = useNavigate();
  const [records, setRecords] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [filter, setFilter] = React.useState('all');
  React.useEffect(function() { fetchRecords(); }, []);
  async function fetchRecords() {
    setLoading(true);
    try {
      const res = await fetch(API_BASE + '/api/purchases/plan/0', { headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) setRecords(await res.json());
    } catch(e) {console.error(e);}
    setLoading(false);
  }
  async function toggleStatus(record) {
    try {
      var suffix = record.status === '待采购' ? '/purchased' : '/unpurchased';
      await fetch(API_BASE + '/api/purchases/' + record.id + suffix, { method: 'POST', headers: { Authorization: 'Bearer ' + token } });
      fetchRecords();
    } catch(e) {console.error(e);}
  }
  function handlePrint() {
    var w = window.open('', '_blank');
    if (!w) { alert('请允许弹出窗口'); return; }
    var needBuy = records.filter(function(r) { return r.status === '待采购'; });
    var bought = records.filter(function(r) { return r.status === '已采购'; });
    var html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>食材采购清单</title>';
    html += '<style>body{font-family:\"Microsoft YaHei\",sans-serif;padding:40px;color:#333}';
    html += 'h1{text-align:center;color:#1f7a4d;margin-bottom:8px}.date{text-align:center;color:#888;font-size:14px;margin-bottom:30px}';
    html += 'h2{color:#c2410c;border-bottom:2px solid #c2410c;padding-bottom:6px;margin-top:24px}';
    html += 'table{width:100%;border-collapse:collapse;margin:16px 0}';
    html += 'th,td{border:1px solid #ddd;padding:10px 14px;text-align:left}th{background:#f4f7f3}';
    html += '.bought h2{color:#1f7a4d;border-color:#1f7a4d}';
    html += '.footer{margin-top:40px;text-align:center;color:#888;font-size:12px}';
    html += '@media print{body{padding:20px}}</style></head><body>';
    html += '<h1>食材采购清单</h1><p class="date">' + new Date().toLocaleDateString('zh-CN') + '</p>';
    html += '<div class="need-buy"><h2>需购食材</h2>';
    if (needBuy.length === 0) { html += '<p>暂无需要采购的食材</p>'; }
    else {
      html += '<table><tr><th>食材名称</th><th>需求数量</th></tr>';
      needBuy.forEach(function(r) { html += '<tr><td>' + r.foodName + '</td><td>' + r.needNum + '</td></tr>'; });
      html += '</table>';
    }
    html += '</div><div class="bought"><h2>已购食材</h2>';
    if (bought.length === 0) { html += '<p>暂无已购食材</p>'; }
    else {
      html += '<table><tr><th>食材名称</th><th>数量</th></tr>';
      bought.forEach(function(r) { html += '<tr><td>' + r.foodName + '</td><td>' + r.needNum + '</td></tr>'; });
      html += '</table>';
    }
    html += '</div><p class="footer">AI膳食预算管理系统 - 采购清单</p></body></html>';
    w.document.write(html);
    w.document.close();
    setTimeout(function() { w.print(); }, 500);
  }
  var needCount = records.filter(function(r) { return r.status === '待采购'; }).length;
  var boughtCount = records.filter(function(r) { return r.status === '已采购'; }).length;
  var filtered = filter === 'all' ? records : records.filter(function(r) { return r.status === (filter === 'need' ? '待采购' : '已采购'); });
  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '加载中...')));
  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: function() { navigate('/'); }, className: 'nav-link' }, '首页'),
        React.createElement('button', { onClick: function() { navigate('/stock'); }, className: 'nav-link' }, '库存')
      ),
      React.createElement('div', { className: 'nav-user' },
        React.createElement('button', { className: 'primary-button small', onClick: handlePrint }, '打印清单')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '采购清单'),
        React.createElement('div', { className: 'header-actions' },
          React.createElement('button', { className: 'ghost-button' + (filter === 'all' ? ' active' : ''), onClick: function() { setFilter('all'); } }, '全部(' + records.length + ')'),
          React.createElement('button', { className: 'ghost-button' + (filter === 'need' ? ' active' : ''), onClick: function() { setFilter('need'); } }, '需购(' + needCount + ')'),
          React.createElement('button', { className: 'ghost-button' + (filter === 'bought' ? ' active' : ''), onClick: function() { setFilter('bought'); } }, '已购(' + boughtCount + ')')
        )
      ),
      filtered.length === 0 ?
        React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '暂无采购记录'),
          React.createElement('p', null, '生成菜谱后缺失食材将自动加入采购清单')
        ) :
        React.createElement('div', { className: 'purchase-list' },
          filtered.map(function(record) {
            return React.createElement('div', { key: record.id, className: 'purchase-item' + (record.status === '已采购' ? ' done' : '') },
              React.createElement('div', { className: 'purchase-check' },
                React.createElement('input', { type: 'checkbox', checked: record.status === '已采购', onChange: function() { toggleStatus(record); } })
              ),
              React.createElement('div', { className: 'purchase-info' },
                React.createElement('span', { className: 'purchase-name' }, record.foodName),
                React.createElement('span', { className: 'purchase-num' }, 'x ' + record.needNum)
              ),
              React.createElement('span', { className: 'purchase-status ' + (record.status === '已采购' ? 'bought' : 'need') }, record.status)
            );
          })
        ),
      React.createElement('div', { className: 'print-tip' },
        React.createElement('p', null, '点击右上角"打印清单"可生成A4纸张适配的打印版本')
      )
    )
  );
}


function splitList(text) {
  return String(text || '').split(/[，,\s]+/).map(s => s.trim()).filter(Boolean);
}

createRoot(document.getElementById('root')).render(<App />);