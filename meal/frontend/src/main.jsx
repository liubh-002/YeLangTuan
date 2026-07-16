import React, { useState, useEffect, createContext, useContext } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useParams } from 'react-router-dom';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE || '';

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
        setUser(prev => ({ ...data, isSubAccount: prev?.isSubAccount || localStorage.getItem('isSubAccount') === 'true' }));
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
    localStorage.setItem('isSubAccount', userData.isSubAccount ? 'true' : 'false');
    sessionStorage.removeItem('mealPlan');
    sessionStorage.removeItem('mealFilters');
    sessionStorage.removeItem('completedMeals');
    fetchMode();
  }

  function logout() {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('isSubAccount');
    sessionStorage.removeItem('mealPlan');
    sessionStorage.removeItem('mealFilters');
    sessionStorage.removeItem('completedMeals');
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
  const [inviteCode, setInviteCode] = useState('');

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
        : inviteCode ? { phone, password, inviteCode } : { phone, password };
      
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (response.ok) {
        if (isRegister) {
          setMessage('注册成功，请登录');
          setIsRegister(false);
        } else {
          const data = await response.json();
          login(data.token, data);
          navigate('/');
        }
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

          <label>邀请码（子账户填写）
            <input type="text" value={inviteCode} onChange={(e) => setInviteCode(e.target.value)} 
                   placeholder="未填写则为主账号" />
          </label>

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
  const currentPath = window.location.pathname;

  return (
    <nav className="top-nav">
      <div className="nav-brand" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="M8 12l2 2 4-4"/></svg>
        <span>AI膳食规划</span>
      </div>
      <div className="nav-links">
        <button onClick={() => navigate('/')} className={`nav-link ${currentPath === '/' ? 'active' : ''}`}>首页</button>
        <button onClick={() => navigate('/stock')} className={`nav-link ${currentPath === '/stock' ? 'active' : ''}`}>库存</button>
        <button onClick={() => navigate('/purchase')} className={`nav-link ${currentPath === '/purchase' ? 'active' : ''}`}>采购</button>
      </div>
      {showUserMenu && user && (
        <div className="nav-user">
          <span className={`mode-badge ${mode.toLowerCase()}`}>{mode === 'FAMILY' ? '家庭模式' : '个人模式'}</span>
          <div className="avatar-menu" onClick={() => setShowMenu(!showMenu)}>
            <div className="avatar">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <span>{user.name} <span style={{fontSize:"11px",color:"#64716b"}}>({user.isSubAccount ? "子账号":"主账号"})</span></span>
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
  const [peopleCount, setPeopleCount] = useState(1);
  const [taste, setTaste] = useState('');
  const [weeklyBudget, setWeeklyBudget] = useState('');
  const [monthlySalary, setMonthlySalary] = useState(user?.monthSalary || '');
  const [crowd, setCrowd] = useState('');
  const [dietTaboo, setDietTaboo] = useState('');
  const [favoriteDishes, setFavoriteDishes] = useState('');
  const [breakfastWant, setBreakfastWant] = useState('');
  const [lunchWant, setLunchWant] = useState('');
  const [dinnerWant, setDinnerWant] = useState('');
  const [customRequirements, setCustomRequirements] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [editingDish, setEditingDish] = useState(null);
  const [editDishName, setEditDishName] = useState('');
  const [showDishDetail, setShowDishDetail] = useState(null);
  const [completedMeals, setCompletedMeals] = useState(() => {
    try { return new Set(JSON.parse(sessionStorage.getItem('completedMeals') || '[]')); } catch(e) { return new Set(); }
  });

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
    // Sync dietTaboo from user profile if not set from saved filters
    if (user && user.dietTaboo && user.dietTaboo !== '无') {
      setDietTaboo(prev => prev || user.dietTaboo);
    }
    // Sync monthlySalary from user profile if not set
    if (user && user.monthSalary) {
      setMonthlySalary(prev => prev || user.monthSalary);
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

  function generateCookingSteps(dishName, ingredients) {
    const steps = [];
    if (dishName.includes('炒') || dishName.includes('爆')) {
      steps.push('第一步：食材准备。将' + ingredients.join('、') + '全部洗净。肉类切成薄片或小块，蔬菜切成丝或片，葱姜蒜切末备用。');
      steps.push('第二步：热锅冷油。锅洗净烧干，倒入适量食用油（约2-3汤匙），烧至六成热（油面微微冒烟）。');
      steps.push('第三步：爆香配料。放入葱姜蒜末，翻炒出香味（约10秒），注意不要炒糊。');
      steps.push('第四步：先炒肉类。放入肉类食材，大火快速翻炒至变色（约1-2分钟），盛出备用。');
      steps.push('第五步：炒蔬菜。锅中留底油，放入蔬菜翻炒至断生（约2-3分钟），可以加少许水防止粘锅。');
      steps.push('第六步：混合翻炒。将炒好的肉类倒回锅中，与蔬菜一起翻炒均匀（约1分钟）。');
      steps.push('第七步：调味出锅。加入盐（约1茶匙）、生抽（约1汤匙）、少许蚝油调味，翻炒均匀后关火，出锅装盘。');
    } else if (dishName.includes('炖') || dishName.includes('煲')) {
      steps.push('第一步：食材处理。将' + ingredients.join('、') + '洗净切块。肉类建议先焯水：冷水下锅，加姜片和料酒，水开后撇去浮沫，捞出洗净。');
      steps.push('第二步：入锅加水。将所有食材放入炖锅或砂锅，加入足量清水（没过食材2-3厘米）。');
      steps.push('第三步：大火烧开。开大火将水烧开，期间不断撇去表面的浮沫，确保汤清澈。');
      steps.push('第四步：转小火炖。水开后转小火，盖上锅盖慢炖。肉类建议炖30-40分钟，排骨等需炖60分钟以上。');
      steps.push('第五步：加蔬菜。出锅前10-15分钟加入蔬菜，继续炖煮至蔬菜软烂。');
      steps.push('第六步：调味出锅。最后加入盐（约1-2茶匙）、少许白胡椒粉调味，搅拌均匀即可关火享用。');
    } else if (dishName.includes('蒸')) {
      steps.push('第一步：食材准备。将' + ingredients.join('、') + '洗净处理好。肉类可以用料酒、生抽腌制10分钟去腥。');
      steps.push('第二步：摆盘。将食材整齐地摆放在盘子里，如果是蒸鱼可以在盘底垫几片姜片。');
      steps.push('第三步：上锅蒸。蒸锅中加水烧开，将盘子放入蒸屉，大火蒸。');
      steps.push('第四步：控制时间。蒸菜时间根据食材而定：蔬菜约8-12分钟，鱼类约10-15分钟，肉类约15-20分钟。');
      steps.push('第五步：出锅调味。取出蒸好的菜，倒掉盘中多余的水分，淋上少许生抽和热油，撒上葱花即可。');
    } else if (dishName.includes('煮')) {
      steps.push('第一步：食材处理。将' + ingredients.join('、') + '洗净。根茎类蔬菜切滚刀块，叶菜洗净备用。');
      steps.push('第二步：煮主料。锅中加水烧开，先放入难熟的食材（如土豆、萝卜等）煮至七分熟（约10-15分钟）。');
      steps.push('第三步：煮配菜。加入叶菜等易熟食材，继续煮2-3分钟。');
      steps.push('第四步：调味。加入盐（约1茶匙）、少许香油或辣椒油调味。');
      steps.push('第五步：出锅。食材煮熟后即可关火，盛入碗中享用。');
    } else if (dishName.includes('汤')) {
      steps.push('第一步：食材处理。将' + ingredients.join('、') + '洗净切块。肉类建议焯水去腥：冷水下锅加姜片，水开后撇沫捞出。');
      steps.push('第二步：煮汤底。锅中加水烧开，放入肉类或骨头，大火烧开后撇去浮沫。');
      steps.push('第三步：慢熬汤底。转小火慢熬20-30分钟，让汤变得浓郁。');
      steps.push('第四步：加蔬菜。放入蔬菜继续煮5-10分钟，至蔬菜变软。');
      steps.push('第五步：调味出锅。加入盐（约1茶匙）、少许白胡椒粉、葱花调味，搅拌均匀后关火。');
    } else if (dishName.includes('煎') || dishName.includes('炸')) {
      steps.push('第一步：食材准备。将' + ingredients.join('、') + '洗净处理好。肉类可以用盐、胡椒粉腌制10分钟。');
      steps.push('第二步：热锅倒油。平底锅烧热，倒入适量食用油（覆盖锅底即可）。');
      steps.push('第三步：煎制。将食材放入锅中，中小火慢煎。');
      steps.push('第四步：翻面。煎至一面金黄后翻面，继续煎另一面。');
      steps.push('第五步：出锅。煎至两面金黄酥脆后，捞出沥油，撒上椒盐或其他调料即可。');
    } else if (dishName.includes('烤')) {
      steps.push('第一步：食材准备。将' + ingredients.join('、') + '洗净切块。');
      steps.push('第二步：腌制。用盐、生抽、料酒、孜然粉等调料腌制食材20分钟。');
      steps.push('第三步：预热烤箱。烤箱预热至200℃。');
      steps.push('第四步：烤制。将食材摆放在烤盘中，放入烤箱烤15-25分钟。');
      steps.push('第五步：翻面。中途可以翻面一次，确保均匀烤熟。');
      steps.push('第六步：出锅。烤至表面金黄酥脆即可取出，撒上葱花或芝麻点缀。');
    } else {
      steps.push('第一步：准备工作。将' + ingredients.join('、') + '全部洗净，根据菜品需要切成合适的形状。');
      steps.push('第二步：烹饪步骤。根据食材特性选择合适的烹饪方式：肉类建议先焯水去腥，蔬菜注意不要煮太久以免营养流失。');
      steps.push('第三步：调味技巧。盐要适量，生抽提鲜，蚝油增加风味，葱姜蒜是万能配料。');
      steps.push('第四步：出锅装盘。烹饪完成后，关火盛出，注意装盘美观。');
    }
    return steps;
  }

  async function handleReplaceIngredient(dayIdx, mealIdx, ingredientIdx) {
    if (!plan) return;
    const meal = plan.days[dayIdx].meals[mealIdx];
    const originalIngredients = meal.ingredients || [];
    let replaceIngredient = '';
    if (ingredientIdx >= 0) {
      replaceIngredient = originalIngredients[ingredientIdx];
    }
    setLoading(true);
    try {
      const response = await fetch(API_BASE + '/api/plans/replace-ingredient', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          dishName: meal.dishName,
          currentIngredients: originalIngredients,
          replaceIngredient: replaceIngredient,
          taste: taste,
          peopleCount: Number(peopleCount)
        })
      });
      if (response.ok) {
        const data = await response.json();
        const updatedPlan = JSON.parse(JSON.stringify(plan));
        updatedPlan.days[dayIdx].meals[mealIdx].ingredients = data.newIngredients;
        updatedPlan.days[dayIdx].meals[mealIdx].dishName = data.newDishName || meal.dishName;
        updatedPlan.days[dayIdx].meals[mealIdx].estimatedCost = data.newCost || meal.estimatedCost;
        setPlan(updatedPlan);
        sessionStorage.setItem('mealPlan', JSON.stringify(updatedPlan));
        setShowDishDetail({ ...showDishDetail, meal: updatedPlan.days[dayIdx].meals[mealIdx] });
      } else {
        const error = await response.json().catch(() => null);
        setMessage(error?.message || '\u66ff\u6362\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5');
      }
    } catch (error) {
      setMessage('\u7f51\u7edc\u8fde\u63a5\u5931\u8d25: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function generatePlan(isSavingMode = false) {
    setLoading(true);
    setMessage('');
    try {
      // Sync dietTaboo and tastePrefer back to user profile
      try {
        await fetch(API_BASE + '/api/users/profile', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
          body: JSON.stringify({ dietTaboo: dietTaboo, tastePrefer: taste })
        });
      } catch(e) {}
      const response = await fetch(API_BASE + '/api/plans/generate', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          peopleCount: Number(peopleCount),
          taste,
          weeklyBudget: Number(weeklyBudget) || 500,
          monthlySalary: Number(monthlySalary || user?.monthSalary || 0),
          crowd,
          avoidIngredients: splitList(dietTaboo),
          favoriteDishes: splitList(favoriteDishes),
          breakfastWant: splitList(breakfastWant),
          lunchWant: splitList(lunchWant),
          dinnerWant: splitList(dinnerWant),
          customRequirements: customRequirements,
          savingMode: isSavingMode
        })
      });
      if (response.ok) {
        const data = await response.json();
        setPlan(data);
        sessionStorage.setItem('mealPlan', JSON.stringify(data));
        // Auto-add missing ingredients to purchase list
        try {
          var allIngredients = [];
          (data.days || []).forEach(function(day) {
            (day.meals || []).forEach(function(meal) {
              (meal.ingredients || []).forEach(function(ing) {
                if (allIngredients.indexOf(ing) === -1) allIngredients.push(ing);
              });
            });
          });
          if (allIngredients.length > 0) {
            await fetch(API_BASE + '/api/purchases/batch-from-ingredients', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
              body: JSON.stringify({ planId: 0, ingredients: allIngredients })
            });
          }
        } catch(e) { console.error('auto purchase failed:', e); }
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
    setLoading(true);
    try {
      const response = await fetch(API_BASE + '/api/plans/edit-dish', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          dayIndex: dayIndex,
          mealIndex: mealIndex,
          newDishName: newName,
          peopleCount: Number(peopleCount),
          taste: taste,
          weeklyBudget: Number(weeklyBudget) || 1000,
          monthlySalary: Number(monthlySalary) || 0,
          avoidIngredients: splitList(dietTaboo),
          favoriteDishes: splitList(favoriteDishes),
          breakfastWant: splitList(breakfastWant),
          lunchWant: splitList(lunchWant),
          dinnerWant: splitList(dinnerWant),
          savingMode: false
        })
      });
      if (response.ok) {
        const data = await response.json();
        setPlan(data);
        sessionStorage.setItem('mealPlan', JSON.stringify(data));
      } else {
        const updatedPlan = JSON.parse(JSON.stringify(plan));
        updatedPlan.days[dayIndex].meals[mealIndex].dishName = newName;
        setPlan(updatedPlan);
        sessionStorage.setItem('mealPlan', JSON.stringify(updatedPlan));
      }
    } catch (error) {
      const updatedPlan = JSON.parse(JSON.stringify(plan));
      updatedPlan.days[dayIndex].meals[mealIndex].dishName = newName;
      setPlan(updatedPlan);
      sessionStorage.setItem('mealPlan', JSON.stringify(updatedPlan));
    } finally {
      setLoading(false);
      setEditingDish(null);
    }
  }

  function handlePrint() { window.print(); }

  async function handleMealComplete(dayName, mealIdx, meal) {
    const mealKey = dayName + '-' + mealIdx;
    const isDone = completedMeals.has(mealKey);
    const next = new Set(completedMeals);
    if (isDone) {
      next["delete"](mealKey);
    } else {
      next.add(mealKey);
    }
    setCompletedMeals(next);
    sessionStorage.setItem('completedMeals', JSON.stringify([...next]));
    if (meal.ingredients && meal.ingredients.length > 0) {
      try {
        var url = isDone ? API_BASE + '/api/stock/restore-batch' : API_BASE + '/api/stock/deduct-batch';
        await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
          body: JSON.stringify(meal.ingredients.map(function(name) { return { foodName: name, quantity: 1 }; }))
        });
      } catch(e) { console.error('库存操作失败:', e); }
    }
  }

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null),
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
            React.createElement('input', { className: 'filter-input', type: 'number', value: weeklyBudget || '', onChange: e => setWeeklyBudget(e.target.value ? Number(e.target.value) : '') })
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
          React.createElement('textarea', { 
            className: 'filter-textarea', 
            value: customRequirements, 
            onChange: e => setCustomRequirements(e.target.value), 
            placeholder: '例如：每天都想吃牛奶，周五晚上想吃红烧鱼，周一到周五营养平衡，周六周日可以稍微享受一些'
          })
        ),
        React.createElement('div', { className: 'filter-section' },
          React.createElement('label', { className: 'filter-label' }, '\u7528\u9910\u4eba\u6570'),
          React.createElement('input', { className: 'filter-input', type: 'number', min: 1, value: peopleCount, onChange: e => setPeopleCount(Number(e.target.value)) })
        ),
        React.createElement('button', { className: 'primary-button', onClick: () => generatePlan(false), disabled: loading, style: { width: '100%', marginTop: '16px' } },
          loading ? '\u6b63\u5728\u751f\u6210...' : 'AI一键生成'
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
            React.createElement('div', { className: 'day-row' },
              (plan.days || []).slice(0, 5).map((day, dayIdx) =>
                React.createElement('article', { key: day.day, className: 'day-card', style: { flex: 1 } },
                  React.createElement('header', null,
                    React.createElement('h3', null, day.day),
                    React.createElement('span', null, '\u00a5' + (day.dailyCost || 0))
                  ),
                  React.createElement('div', { style: { flex: 1, display: 'flex', flexDirection: 'column' } },
                    (day.meals || []).map((meal, mealIdx) =>
                      React.createElement('div', { key: day.day + '-' + meal.mealType, className: 'meal-row' + (completedMeals.has(day.day + '-' + mealIdx) ? ' done' : ''), style: { flex: 1 } },
                        React.createElement('div', { className: 'meal-top' },
                          React.createElement('input', { type: 'checkbox', className: 'meal-done-checkbox', checked: completedMeals.has(day.day + '-' + mealIdx), onChange: function() { handleMealComplete(day.day, mealIdx, meal); } }),
                          React.createElement('strong', { className: completedMeals.has(day.day + '-' + mealIdx) ? 'meal-type done' : 'meal-type' }, meal.mealType),
                          React.createElement('span', { className: 'meal-price' }, '\u00a5' + (meal.estimatedCost || 0))
                        ),
                        editingDish && editingDish.dayIndex === dayIdx && editingDish.mealIndex === mealIdx ?
                        React.createElement('div', { className: 'dish-edit-inline' },
                          React.createElement('input', { className: 'filter-input', value: editDishName, onChange: e => setEditDishName(e.target.value), style: { flex: 1 } }),
                          React.createElement('button', { className: 'primary-button small', onClick: () => { editDish(dayIdx, mealIdx, editDishName); } }, '\u4fdd\u5b58'),
                          React.createElement('button', { className: 'ghost-button small', onClick: () => setEditingDish(null) }, '\u53d6\u6d88')
                        ) :
                        React.createElement(React.Fragment, null,
                          React.createElement('h4', { style: { cursor: 'pointer', margin: '4px 0', fontSize: '13px' }, onClick: () => setShowDishDetail({ day: day, meal: meal, dayIdx: dayIdx, mealIdx: mealIdx }) }, meal.dishName),
                          React.createElement('p', { style: { margin: '2px 0', fontSize: '12px', color: '#64716b' } }, (meal.ingredients || []).join(' / ')),
                          React.createElement('div', { className: 'edit-buttons', style: { marginTop: '4px' } },
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
            React.createElement('div', { className: 'day-row' },
              (plan.days || []).slice(5, 7).map((day, dayIdx) =>
                React.createElement('article', { key: day.day, className: 'day-card', style: { flex: 0.55 } },
                  React.createElement('header', null,
                    React.createElement('h3', null, day.day),
                    React.createElement('span', null, '\u00a5' + (day.dailyCost || 0))
                  ),
                  React.createElement('div', { style: { flex: 1, display: 'flex', flexDirection: 'column' } },
                    (day.meals || []).map((meal, mealIdx) =>
                      React.createElement('div', { key: day.day + '-' + meal.mealType, className: 'meal-row' + (completedMeals.has(day.day + '-' + mealIdx) ? ' done' : ''), style: { flex: 1 } },
                        React.createElement('div', { className: 'meal-top' },
                          React.createElement('input', { type: 'checkbox', className: 'meal-done-checkbox', checked: completedMeals.has(day.day + '-' + mealIdx), onChange: function() { handleMealComplete(day.day, mealIdx, meal); } }),
                          React.createElement('strong', { className: completedMeals.has(day.day + '-' + mealIdx) ? 'meal-type done' : 'meal-type' }, meal.mealType),
                          React.createElement('span', { className: 'meal-price' }, '\u00a5' + (meal.estimatedCost || 0))
                        ),
                        editingDish && editingDish.dayIndex === (dayIdx + 5) && editingDish.mealIndex === mealIdx ?
                        React.createElement('div', { className: 'dish-edit-inline' },
                          React.createElement('input', { className: 'filter-input', value: editDishName, onChange: e => setEditDishName(e.target.value), style: { flex: 1 } }),
                          React.createElement('button', { className: 'primary-button small', onClick: () => { editDish(dayIdx + 5, mealIdx, editDishName); } }, '\u4fdd\u5b58'),
                          React.createElement('button', { className: 'ghost-button small', onClick: () => setEditingDish(null) }, '\u53d6\u6d88')
                        ) :
                        React.createElement(React.Fragment, null,
                          React.createElement('h4', { style: { cursor: 'pointer', margin: '4px 0', fontSize: '13px' }, onClick: () => setShowDishDetail({ day: day, meal: meal, dayIdx: dayIdx + 5, mealIdx: mealIdx }) }, meal.dishName),
                          React.createElement('p', { style: { margin: '2px 0', fontSize: '12px', color: '#64716b' } }, (meal.ingredients || []).join(' / ')),
                          React.createElement('div', { className: 'edit-buttons', style: { marginTop: '4px' } },
                            React.createElement('button', { className: 'ghost-button small', onClick: () => { setEditingDish({ dayIndex: dayIdx + 5, mealIndex: mealIdx }); setEditDishName(meal.dishName); } }, '\u4fee\u6539'),
                            React.createElement('button', { className: 'ghost-button small', onClick: () => setShowDishDetail({ day: day, meal: meal, dayIdx: dayIdx + 5, mealIdx: mealIdx }) }, '\u8be6\u60c5')
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        showDishDetail ? React.createElement('div', { className: 'modal-overlay', onClick: () => setShowDishDetail(null) },
          React.createElement('div', { className: 'modal-content', onClick: e => e.stopPropagation(), style: { maxWidth: '700px', maxHeight: '80vh', overflowY: 'auto' } },
            React.createElement('h3', null, showDishDetail.meal.dishName),
            React.createElement('p', { style: { color: '#64716b', marginBottom: '12px' } }, showDishDetail.meal.mealType + ' \u00a5' + (showDishDetail.meal.estimatedCost || 0)),
            React.createElement('div', { className: 'dish-section' },
              React.createElement('h4', null, '\u6240\u9700\u98df\u6750'),
              React.createElement('div', { className: 'ingredient-list' },
                (showDishDetail.meal.ingredients || []).map((ing, i) => React.createElement('div', { key: i, className: 'ingredient-item' },
                  React.createElement('span', null, ing),
                  React.createElement('button', { className: 'ghost-button small', onClick: () => handleReplaceIngredient(showDishDetail.dayIdx, showDishDetail.mealIdx, i) }, '\u66ff\u6362')
                ))
              )
            ),
            React.createElement('div', { className: 'dish-section' },
              React.createElement('h4', null, '\u8be6\u7ec6\u505a\u6cd5'),
              React.createElement('div', { className: 'cooking-steps' },
                generateCookingSteps(showDishDetail.meal.dishName, showDishDetail.meal.ingredients || []).map((step, i) => React.createElement('div', { key: i, className: 'cooking-step' },
                  React.createElement('span', { className: 'step-number' }, i + 1),
                  React.createElement('span', { className: 'step-text' }, step)
                ))
              )
            ),
            React.createElement('div', { className: 'dish-section' },
              React.createElement('h4', null, '\u8425\u517b\u8bf4\u660e'),
              React.createElement('p', null, showDishDetail.meal.nutritionNote || '\u6682\u65e0\u8be6\u7ec6\u4fe1\u606f'),
              showDishDetail.meal.savingNote ? React.createElement('p', { style: { color: '#1f7a4d', fontSize: '13px', marginTop: '8px' } }, showDishDetail.meal.savingNote) : null
            ),
            React.createElement('div', { className: 'modal-actions' },
              React.createElement('button', { className: 'ghost-button', onClick: () => handleReplaceIngredient(showDishDetail.dayIdx, showDishDetail.mealIdx, -1) }, 'AI一键替换食材'),
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
  const { user, token } = useUser();
  const navigate = useNavigate();
  const [formData, setFormData] = React.useState({
    name: user?.name || "",
    gender: user?.gender || "",
    age: user?.age || "",
    monthSalary: user?.monthSalary || "",
    tastePrefer: user?.tastePrefer || "",
    dietTaboo: user?.dietTaboo || ""
  });
  const [message, setMessage] = React.useState("");
  React.useEffect(() => { fetchProfile(); }, []);
  async function fetchProfile() {
    try {
      const res = await fetch(API_BASE + "/api/users/profile", {
        headers: { Authorization: "Bearer " + token }
      });
      if (res.ok) {
        const data = await res.json();
        setFormData({ name: data.name || "", gender: data.gender || "", age: data.age || "", monthSalary: data.monthSalary || "", tastePrefer: data.tastePrefer || "", dietTaboo: data.dietTaboo || "" });
      }
    } catch(e) {}
  }
  async function handleSaveProfile(e) {
    e.preventDefault();
    try {
      const res = await fetch(API_BASE + "/api/users/profile", {
        method: "PUT",
        headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });
      setMessage(res.ok ? "保存成功" : "保存失败");
      if (!res.ok) setTimeout(() => setMessage(""), 2000);
    } catch(e) { setMessage("网络错误"); }
  }
  return React.createElement("div", { className: "app-container" },
    React.createElement(TopNav, null),
    React.createElement("main", { className: "page-content" },
      message ? React.createElement("p", { style: { color: "#1f7a4d", marginBottom: "12px" } }, message) : null,
      React.createElement("div", { className: "form-grid" },
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "姓名"),
          React.createElement("input", { className: "filter-input", value: formData.name, onChange: e => setFormData({...formData, name: e.target.value}) })),
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "性别"),
          React.createElement("select", { className: "filter-select", value: formData.gender, onChange: e => setFormData({...formData, gender: e.target.value}) },
            React.createElement("option", { value: "" }, "请选择"),
            React.createElement("option", { value: "男" }, "男"),
            React.createElement("option", { value: "女" }, "女"))),
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "年龄"),
          React.createElement("input", { className: "filter-input", type: "number", value: formData.age, onChange: e => setFormData({...formData, age: e.target.value}) })),
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "月度工资(元)"),
          React.createElement("input", { className: "filter-input", type: "number", value: formData.monthSalary, onChange: e => setFormData({...formData, monthSalary: e.target.value}) })),
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "饮食忌口"),
          React.createElement("input", { className: "filter-input", value: formData.dietTaboo, onChange: e => setFormData({...formData, dietTaboo: e.target.value}) })),
        React.createElement("div", { className: "form-group" },
          React.createElement("label", null, "喜欢的菜品"),
          React.createElement("input", { className: "filter-input", value: formData.tastePrefer, onChange: e => setFormData({...formData, tastePrefer: e.target.value}) }))
      ),
      React.createElement("button", { className: "primary-button", onClick: handleSaveProfile, style: { marginTop: "16px", width: "100%" } }, "保存修改")
    )
  );
}
function FamilyMembersPage() {
  const { user, token, mode, fetchMode } = useUser();
  const navigate = useNavigate();
  const [family, setFamily] = React.useState(null);
  const [familyMembers, setFamilyMembers] = React.useState([]);
  const [pendingApprovals, setPendingApprovals] = React.useState([]);
  const [joinCode, setJoinCode] = React.useState('');
  const [inviteCode, setInviteCode] = React.useState('');
  const [loading, setLoading] = React.useState(true);
  const [isMainAccount, setIsMainAccount] = React.useState(false);
  const [subAccountForm, setSubAccountForm] = React.useState({ name: '', age: '', phone: '', password: '', personTag: '\u666e\u901a', dietTaboo: '\u65e0', appetite: 3 });
  const [showAddSubAccount, setShowAddSubAccount] = React.useState(false);

  React.useEffect(function() {
    fetchFamily();
    fetchMainAccountStatus();
  }, []);

  async function fetchMainAccountStatus() {
    try {
      const res = await fetch(API_BASE + '/api/family/check-main-account', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) {
        const data = await res.json();
        setIsMainAccount(data.isMainAccount);
        if (data.isMainAccount) fetchPendingApprovals();
      }
    } catch(e) {}
  }

  async function fetchPendingApprovals() {
    try {
      const res = await fetch(API_BASE + '/api/family/pending-approvals', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) setPendingApprovals(await res.json());
    } catch(e) {}
  }

  async function fetchFamily() {
    setLoading(true);
    try {
      const res = await fetch(API_BASE + '/api/family/group', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) {
        const data = await res.json();
        setInviteCode(data.inviteCode || '');
        setFamily(data);
        if (data.pendingApprovals) setPendingApprovals(data.pendingApprovals);
      }
    } catch(e) {}
    try {
      const res = await fetch(API_BASE + '/api/family/members', {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.ok) setFamilyMembers(await res.json());
    } catch(e) {}
    setLoading(false);
  }

  async function handleCreateFamily() {
    try {
      const res = await fetch(API_BASE + '/api/family/group', { method: 'POST', headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) { const d = await res.json(); setInviteCode(d.inviteCode); fetchFamily(); fetchMode(); fetchMainAccountStatus(); }
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

  async function handleAddSubAccount() {
    if (!subAccountForm.name || !subAccountForm.phone || !subAccountForm.password) {
      alert('\u59d3\u540d\u3001\u624b\u673a\u53f7\u548c\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(subAccountForm.phone)) {
      alert('\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e');
      return;
    }
    try {
      const res = await fetch(API_BASE + '/api/family/sub-account', { method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...subAccountForm, age: Number(subAccountForm.age) || null }) });
      if (res.ok) { setShowAddSubAccount(false); setSubAccountForm({ name: '', age: '', phone: '', password: '', personTag: '\u666e\u901a', dietTaboo: '\u65e0', appetite: 3 }); fetchFamily(); }
      else { const err = await res.json(); alert(err.message || '\u6dfb\u52a0\u5931\u8d25'); }
    } catch(e) { alert('\u7f51\u7edc\u9519\u8bef'); }
  }

  async function handleApproveMember(memberId) {
    try {
      const res = await fetch(API_BASE + '/api/family/approve/' + memberId, { method: 'POST',
        headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) { fetchFamily(); fetchMainAccountStatus(); }
      else { const err = await res.json(); alert(err.message || '\u5ba1\u6279\u5931\u8d25'); }
    } catch(e) { alert('\u7f51\u7edc\u9519\u8bef'); }
  }

  async function handleRejectMember(memberId) {
    try {
      await fetch(API_BASE + '/api/family/reject/' + memberId, { method: 'POST',
        headers: { Authorization: 'Bearer ' + token } });
      fetchFamily(); fetchMainAccountStatus();
    } catch(e) { alert('\u7f51\u7edc\u9519\u8bef'); }
  }

  async function handleDeleteMember(id) {
    if (!confirm('\u786e\u5b9a\u5220\u9664\u8be5\u6210\u5458\uff1f')) return;
    try { await fetch(API_BASE + '/api/family/members/' + id, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } }); fetchFamily(); } catch(e) {}
  }

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '\u52a0\u8f7d\u4e2d...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '\u5bb6\u5ead\u7ba1\u7406'),
        React.createElement('span', { style: { fontSize: '12px', color: '#64716b' } }, (user && user.isSubAccount) ? '\u5b50\u8d26\u53f7' : '\u4e3b\u8d26\u53f7')
      ),
      !inviteCode ?
        React.createElement('div', { style: { textAlign: 'center', padding: '40px 20px' } },
          React.createElement('p', null, '\u60a8\u5c1a\u672a\u521b\u5efa\u5bb6\u5ead\u7ec4'),
          (user && !user.isSubAccount) ?
            React.createElement('button', { className: 'primary-button', onClick: handleCreateFamily }, '\u521b\u5efa\u5bb6\u5ead') :
            React.createElement('div', null,
              React.createElement('p', { style: { color: '#64716b', fontSize: '13px' } }, '\u8bf7\u901a\u8fc7\u9080\u8bf7\u7801\u52a0\u5165\u5bb6\u5ead'),
              React.createElement('div', { className: 'form-row', style: { marginTop: '12px', justifyContent: 'center' } },
                React.createElement('input', { className: 'filter-input', value: joinCode, onChange: e => setJoinCode(e.target.value), placeholder: '\u8f93\u5165\u9080\u8bf7\u7801', style: { flex: 1, maxWidth: '300px' } }),
                React.createElement('button', { className: 'primary-button', onClick: handleJoinFamily, disabled: !joinCode.trim() }, '\u52a0\u5165')
              )
            )
        ) :
        React.createElement('div', null,
          React.createElement('div', { className: 'invite-code-box' },
            React.createElement('span', null, '\u9080\u8bf7\u7801:'),
            React.createElement('span', { className: 'code' }, inviteCode),
            React.createElement('button', { className: 'ghost-button small', onClick: () => { navigator.clipboard.writeText(inviteCode); alert('\u5df2\u590d\u5236'); } }, '\u590d\u5236')
          ),
          (user && !user.isSubAccount) ?
            React.createElement('div', { style: { marginTop: '16px', marginBottom: '16px' } },
              React.createElement('button', { className: 'primary-button small', style: { background: '#7c3aed' }, onClick: () => setShowAddSubAccount(true) }, '\u6dfb\u52a0\u5b50\u8d26\u53f7')
            ) : null,
          showAddSubAccount ?
            React.createElement('div', { className: 'modal-overlay', onClick: () => setShowAddSubAccount(false) },
              React.createElement('div', { className: 'modal-content', onClick: function(e) { e.stopPropagation(); } },
                React.createElement('h3', null, '\u6dfb\u52a0\u5b50\u8d26\u53f7'),
                React.createElement('p', { style: { fontSize: '13px', color: '#64716b', marginBottom: '12px' } }, '\u8bbe\u7f6e\u5b50\u8d26\u53f7\u7684\u8d26\u53f7\u5bc6\u7801\uff0c\u5b50\u8d26\u53f7\u53ef\u4f7f\u7528\u624b\u673a\u53f7+\u5bc6\u7801+\u9080\u8bf7\u7801\u767b\u5f55'),
                React.createElement('div', { className: 'form-group' },
                  React.createElement('label', null, '\u59d3\u540d'),
                  React.createElement('input', { className: 'filter-input', value: subAccountForm.name, onChange: e => setSubAccountForm({...subAccountForm, name: e.target.value}) })),
                React.createElement('div', { className: 'form-group' },
                  React.createElement('label', null, '\u624b\u673a\u53f7'),
                  React.createElement('input', { className: 'filter-input', value: subAccountForm.phone, onChange: e => setSubAccountForm({...subAccountForm, phone: e.target.value}), placeholder: '11\u4f4d\u624b\u673a\u53f7' })),
                React.createElement('div', { className: 'form-group' },
                  React.createElement('label', null, '\u5bc6\u7801'),
                  React.createElement('input', { className: 'filter-input', type: 'password', value: subAccountForm.password, onChange: e => setSubAccountForm({...subAccountForm, password: e.target.value}) })),
                React.createElement('div', { className: 'form-row' },
                  React.createElement('div', { className: 'form-group' },
                    React.createElement('label', null, '\u5e74\u9f84'),
                    React.createElement('input', { className: 'filter-input', type: 'number', value: subAccountForm.age, onChange: e => setSubAccountForm({...subAccountForm, age: e.target.value}) })),
                  React.createElement('div', { className: 'form-group' },
                    React.createElement('label', null, '\u4eba\u7fa4\u6807\u7b7e'),
                    React.createElement('select', { className: 'filter-select', value: subAccountForm.personTag, onChange: e => setSubAccountForm({...subAccountForm, personTag: e.target.value}) },
                      React.createElement('option', { value: '\u666e\u901a' }, '\u666e\u901a'),
                      React.createElement('option', { value: '\u513f\u7ae5' }, '\u513f\u7ae5'),
                      React.createElement('option', { value: '\u9752\u5e74' }, '\u9752\u5e74'),
                      React.createElement('option', { value: '\u8001\u5e74' }, '\u8001\u5e74'),
                      React.createElement('option', { value: '\u75c5\u4eba' }, '\u75c5\u4eba'),
                      React.createElement('option', { value: '\u51cf\u80a5' }, '\u51cf\u80a5'))),
                ),
                React.createElement('div', { className: 'form-group' },
                  React.createElement('label', null, '\u996e\u98df\u5fcc\u53e3'),
                  React.createElement('input', { className: 'filter-input', value: subAccountForm.dietTaboo, onChange: e => setSubAccountForm({...subAccountForm, dietTaboo: e.target.value}) })),
                React.createElement('div', { className: 'modal-actions' },
                  React.createElement('button', { className: 'ghost-button', onClick: () => setShowAddSubAccount(false) }, '\u53d6\u6d88'),
                  React.createElement('button', { className: 'primary-button', onClick: handleAddSubAccount }, '\u521b\u5efa\u5b50\u8d26\u53f7'))
              )
            ) : null,
          (user && !user.isSubAccount) && pendingApprovals.length > 0 ?
            React.createElement('div', { className: 'section-header', style: { marginTop: '16px' } },
              React.createElement('h3', { style: { margin: 0, color: '#c2410c' } }, '\u5f85\u5ba1\u6279\u7533\u8bf7 (' + pendingApprovals.length + ')'),
              React.createElement('div', { className: 'pending-list', style: { width: '100%', marginTop: '8px' } },
                pendingApprovals.map(function(m) {
                  return React.createElement('div', { key: m.id, className: 'member-card', style: { border: '1px solid #f5c6a0', background: '#fff5ee' } },
                    React.createElement('div', { className: 'member-info' },
                      React.createElement('strong', null, m.name || '\u5f85\u5b8c\u5584'),
                      React.createElement('span', null, m.phone ? '\u624b\u673a:' + m.phone : '')
                    ),
                    React.createElement('div', { className: 'edit-buttons' },
                      React.createElement('button', { className: 'primary-button small', onClick: () => handleApproveMember(m.id) }, '\u540c\u610f'),
                      React.createElement('button', { className: 'ghost-button small danger', onClick: () => handleRejectMember(m.id) }, '\u62d2\u7edd')
                    )
                  );
                })
              )
            ) : null,
          React.createElement('div', { className: 'section-header', style: { marginTop: '16px', marginBottom: '12px' } },
            React.createElement('h3', { style: { margin: 0 } }, '\u5bb6\u5ead\u6210\u5458 (' + familyMembers.length + ')')
          ),
          React.createElement('div', { className: 'member-list' },
            familyMembers.map(function(m) {
              return React.createElement('div', { key: m.id, className: 'member-card' },
                React.createElement('div', { className: 'member-info' },
                  React.createElement('strong', null, m.name),
                  React.createElement('span', null,
                    (m.isSubAccount ? '\u5b50\u8d26\u53f7 | ' : '') + (m.personTag || '') +
                    (m.dietTaboo && m.dietTaboo !== '\u65e0' ? ' | \u5fcc\u53e3:' + m.dietTaboo : ' | \u65e0\u5fcc\u53e3')
                  ),
                  m.phone ? React.createElement('span', { style: { fontSize: '12px', color: '#64716b' } }, '\u624b\u673a:' + m.phone) : null
                ),
                m.isSubAccount ?
                  React.createElement('button', { className: 'ghost-button small danger', onClick: () => handleDeleteMember(m.id) }, '\u5220\u9664') :
                  null
              );
            }),
            familyMembers.length === 0 ? React.createElement('p', { style: { color: '#64716b', textAlign: 'center' } }, '\u6682\u65e0\u5bb6\u5ead\u6210\u5458') : null
          )
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
    React.createElement(TopNav, null),
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
    React.createElement(TopNav, null),
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
    React.createElement(TopNav, null),
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

  async function handleDeleteAll() {
    if (!confirm('确定要删除所有库存吗？')) return;
    try {
      await fetch(API_BASE + '/api/stock/all', { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } });
      fetchStocks();
    } catch(e) {console.error(e);}
  }

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '\u52a0\u8f7d\u4e2d...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '\u98df\u6750\u5e93\u5b58\u7ba1\u7406'),
        React.createElement('button', { className: 'primary-button small', onClick: () => { setShowAdd(true); setAddForm({ foodName: '', unit: '\u65a4', stockNum: '' }); } }, '\u65b0\u589e\u98df\u6750'),
                React.createElement('button', { className: 'ghost-button small danger', onClick: handleDeleteAll, style: { marginLeft: '8px' } }, '\u5220\u9664\u6240\u6709'),
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
    React.createElement(TopNav, null),
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
  async function updateQuantity(record, delta) {
    var newNum = Math.max(1, (record.needNum || 1) + delta);
    try {
      await fetch(API_BASE + '/api/purchases/' + record.id + '/quantity', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
        body: JSON.stringify({ quantity: newNum })
      });
      fetchRecords();
    } catch(e) { console.error(e); }
  }

  async function handleDeleteAll() {
    if (!confirm('确定要删除所有采购记录吗？')) return;
    try {
      await fetch(API_BASE + '/api/purchases/all', { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } });
      fetchRecords();
    } catch(e) {console.error(e);}
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
    React.createElement(TopNav, null),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'section-header' },
        React.createElement('h2', null, '采购清单'),
        React.createElement('div', { className: 'header-actions' },
          React.createElement('button', { className: 'ghost-button' + (filter === 'all' ? ' active' : ''), onClick: function() { setFilter('all'); } }, '全部(' + records.length + ')'),
          React.createElement('button', { className: 'ghost-button' + (filter === 'need' ? ' active' : ''), onClick: function() { setFilter('need'); } }, '需购(' + needCount + ')'),
          React.createElement('button', { className: 'ghost-button' + (filter === 'bought' ? ' active' : ''), onClick: function() { setFilter('bought'); } }, '已购(' + boughtCount + ')'),
          React.createElement('button', { className: 'primary-button small', style: { marginLeft: '8px' }, onClick: function() { handlePrint(); } }, '打印采购清单'),
          React.createElement('button', { className: 'ghost-button small danger', onClick: handleDeleteAll, style: { marginLeft: '8px' } }, '一键删除')
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
                React.createElement('div', { className: 'quantity-selector' },
                  React.createElement('button', { className: 'qty-btn', onClick: function() { updateQuantity(record, -1); } }, '-'),
                  React.createElement('span', { className: 'qty-value' }, record.needNum),
                  React.createElement('button', { className: 'qty-btn', onClick: function() { updateQuantity(record, 1); } }, '+')
                )
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
