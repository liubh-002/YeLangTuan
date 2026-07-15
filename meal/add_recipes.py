with open(r'D:\test\practice\frontend\src\main.jsx', 'r', encoding='utf-8') as f:
    content = f.read()

fm_idx = content.find('function FamilyMembersPage()')
sp_idx = content.find('function StockPage()', fm_idx)
print(f"FamilyMembersPage at {fm_idx}, StockPage at {sp_idx}")

recipe_page = '''

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
        try { setRecipes(JSON.parse(text)); } catch { setRecipes([]); }
      }
    } catch(e) {}
    setLoading(false);
  }

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\\u9996\\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, '\\u83dc\\u8c31\\u641c\\u7d22'),
      React.createElement('div', { className: 'search-box' },
        React.createElement('input', { value: keyword, onChange: e => setKeyword(e.target.value), placeholder: '\\u8f93\\u5165\\u98df\\u6750\\u540d\\u79f0\\u641c\\u7d22\\u83dc\\u8c31...', onKeyDown: e => { if (e.key === 'Enter') search(); } }),
        React.createElement('button', { className: 'primary-button', onClick: search, disabled: loading }, loading ? '\\u641c\\u7d22\\u4e2d...' : '\\u641c\\u7d22')
      ),
      recipes.length === 0 && !loading ?
        React.createElement('div', { className: 'empty-state' },
          React.createElement('h2', null, '\\u8f93\\u5165\\u98df\\u6750\\u540d\\u79f0\\u641c\\u7d22\\u83dc\\u8c31'),
          React.createElement('p', null, '\\u6bd4\\u5982\\uff1a\\u9e21\\u86cb\\u3001\\u7ffb\\u8304\\u3001\\u571f\\u8c46...')
        ) :
        React.createElement('div', { className: 'recipe-list' },
          recipes.map((r, idx) => React.createElement('div', { key: idx, className: 'recipe-card' },
            React.createElement('h3', null, r.name),
            React.createElement('div', { className: 'recipe-tags' }, r.category ? React.createElement('span', { className: 'tag' }, r.category) : null),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\\u98df\\u6750'),
              React.createElement('span', null, (r.ingredients || []).join(' / '))
            ),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\\u505a\\u6cd5'),
              React.createElement('ol', null, (r.steps || []).map((s, i) => React.createElement('li', { key: i }, s)))
            )
          ))
        )
      )
    )
  );
}

function RecipeCategoryPage() {
  const { token } = useUser();
  const { category } = useParams();
  const navigate = useNavigate();
  const [recipes, setRecipes] = React.useState([]);

  React.useEffect(() => {
    async function load() {
      try {
        const mockData = { diet: [{ name: '\\u51cf\\u80a5\\u68f1\\u89d2\\u7092\\u9e21\\u80f8', category: '\\u51cf\\u80a5', ingredients: ['\\u68f1\\u89d2', '\\u9e21\\u80f8\\u8089', '\\u5927\\u849c'], steps: ['\\u68f1\\u89d2\\u6d17\\u51c0\\u5207\\u6bb5', '\\u9e21\\u80f8\\u5207\\u4e1d\\u7528\\u6599\\u9152\\u814c\\u5236', '\\u7092\\u9e21\\u80f8\\u53d8\\u8272\\u76db\\u51fa', '\\u7092\\u68f1\\u89d2\\u81f3\\u7eff', '\\u5012\\u56de\\u9e21\\u80f8\\u7ffb\\u7092\\u8c03\\u5473'] }] };
        setRecipes(mockData[category] || []);
      } catch(e) {}
    }
    load();
  }, [category]);

  const categoryNames = { diet: '\\u51cf\\u80a5\\u83dc\\u8c31', patient: '\\u75c5\\u4eba\\u83dc\\u8c31', teen: '\\u9752\\u5c11\\u5e74\\u83dc\\u8c31', elder: '\\u8001\\u5e74\\u4eba\\u83dc\\u8c31' };

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\\u9996\\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, categoryNames[category] || '\\u83dc\\u8c31'),
      recipes.length > 0 ?
        React.createElement('div', { className: 'recipe-list' },
          recipes.map((r, idx) => React.createElement('div', { key: idx, className: 'recipe-card' },
            React.createElement('h3', null, r.name),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\\u98df\\u6750'),
              React.createElement('p', null, (r.ingredients || []).join(', '))
            ),
            React.createElement('div', { className: 'recipe-section' },
              React.createElement('h4', null, '\\u505a\\u6cd5'),
              React.createElement('ol', null, (r.steps || []).map((s, i) => React.createElement('li', { key: i }, s)))
            )
          ))
        ) :
        React.createElement('div', { className: 'empty-state' }, React.createElement('h2', null, '\\u6682\\u65e0\\u83dc\\u8c31\\u6570\\u636e'))
    )
  );
}

function AdminPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState('test');

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\\u8fd4\\u56de\\u9996\\u9875')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('div', { className: 'admin-tabs' },
        React.createElement('button', { className: 'tab-btn' + (activeTab === 'test' ? ' active' : ''), onClick: () => setActiveTab('test') }, 'AI\\u6d4b\\u8bd5\\u9762\\u677f')
      ),
      activeTab === 'test' ?
        React.createElement('div', { className: 'admin-content' },
          React.createElement('h3', null, 'AI\\u81ea\\u52a8\\u5316\\u6d4b\\u8bd5\\u9762\\u677f'),
          React.createElement('p', null, '\\u81ea\\u5b9a\\u4e49\\u7b5b\\u9009\\u53c2\\u6570\\u4e00\\u952e\\u6267\\u884cAI\\u751f\\u6210\\uff0c\\u53ef\\u89c6\\u5316\\u8f93\\u51faFC/Skill/MCP/Agent\\u5b8c\\u6574\\u8c03\\u7528\\u65e5\\u5fd7'),
          React.createElement('div', { className: 'test-scenarios' },
            React.createElement('div', { className: 'scenario-card' },
              React.createElement('h4', null, '\\u591a\\u6210\\u5458\\u6df7\\u5408\\u5bb6\\u5ead\\u6d4b\\u8bd5'),
              React.createElement('p', null, '\\u6d4b\\u8bd5Skill4\\u591a\\u6210\\u5458\\u6df7\\u5408\\u996e\\u98df\\u9002\\u914d\\u529f\\u80fd'),
              React.createElement('button', { className: 'primary-button' }, '\\u8fd0\\u884c\\u6d4b\\u8bd5')
            ),
            React.createElement('div', { className: 'scenario-card' },
              React.createElement('h4', null, '\\u81ea\\u5b9a\\u4e49\\u83dc\\u54c1\\u9884\\u7b97\\u91cd\\u7b97'),
              React.createElement('p', null, '\\u6d4b\\u8bd5Skill5\\u81ea\\u5b9a\\u4e49\\u83dc\\u54c1\\u9884\\u7b97\\u91cd\\u7b97\\u529f\\u80fd'),
              React.createElement('button', { className: 'primary-button' }, '\\u8fd0\\u884c\\u6d4b\\u8bd5')
            )
          )
        ) : null
    )
  );
}

'''

content = content[:sp_idx] + recipe_page + content[sp_idx:]

with open(r'D:\test\practice\frontend\src\main.jsx', 'w', encoding='utf-8') as f:
    f.write(content)
print('Added RecipeSearch, RecipeCategory, Admin pages')
