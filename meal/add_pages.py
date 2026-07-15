with open(r'D:\test\practice\frontend\src\main.jsx', 'r', encoding='utf-8') as f:
    content = f.read()

# Find insertion point: after printMenu closing, before StockPage
pm_start = content.find('function printMenu()')
pm_first_brace = content.find('{', pm_start)
pm_close = content.find('}', pm_first_brace)
stock_start = content.find('function StockPage()')

print(f"printMenu start={pm_start}, close={pm_close}")
print(f"StockPage start={stock_start}")

# Build ProfilePage (with family management from previous version)
profile_page = '''
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
  const [memberForm, setMemberForm] = React.useState({ name: '', age: '', personTag: '\\u666e\\u901a', appetite: 3, dietTaboo: '\\u65e0' });
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
      setMessage(res.ok ? '\\u4fdd\\u5b58\\u6210\\u529f' : '\\u4fdd\\u5b58\\u5931\\u8d25');
      if (!res.ok) setTimeout(() => setMessage(''), 2000);
    } catch { setMessage('\\u7f51\\u7edc\\u9519\\u8bef'); }
  }

  async function handleCreateFamily() {
    try {
      const res = await fetch(API_BASE + '/api/family/group', { method: 'POST', headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) { const d = await res.json(); setInviteCode(d.inviteCode); fetchFamily(); fetchMode(); }
    } catch(e) { alert('\\u521b\\u5efa\\u5931\\u8d25'); }
  }

  async function handleJoinFamily() {
    if (!joinCode.trim()) return;
    try {
      const res = await fetch(API_BASE + '/api/family/join', { method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify({ inviteCode: joinCode.trim() }) });
      if (res.ok) { setJoinCode(''); fetchFamily(); fetchMode(); } else { const err = await res.json(); alert(err.message || '\\u52a0\\u5165\\u5931\\u8d25'); }
    } catch(e) { alert('\\u7f51\\u7edc\\u9519\\u8bef'); }
  }

  async function handleAddMember() {
    if (!memberForm.name || !memberForm.age) return;
    try {
      const res = await fetch(API_BASE + '/api/family/members', { method: 'POST',
        headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify(memberForm) });
      if (res.ok) { setShowAddMember(false); setMemberForm({ name: '', age: '', personTag: '\\u666e\\u901a', appetite: 3, dietTaboo: '\\u65e0' }); fetchFamily(); }
    } catch(e) { alert('\\u6dfb\\u52a0\\u5931\\u8d25'); }
  }

  async function handleDeleteMember(id) {
    if (!confirm('\\u786e\\u5b9a\\u5220\\u9664\\u8be5\\u6210\\u5458\\uff1f')) return;
    try { await fetch(API_BASE + '/api/family/members/' + id, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } }); fetchFamily(); } catch(e) {}
  }

  return (
    React.createElement('div', { className: 'app-container' },
      React.createElement(TopNav, null,
        React.createElement('div', { className: 'nav-links' },
          React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\\u9996\\u9875'),
          React.createElement('button', { onClick: () => navigate('/stock'), className: 'nav-link' }, '\\u5e93\\u5b58'),
          React.createElement('button', { onClick: () => navigate('/bill'), className: 'nav-link' }, '\\u8bb0\\u8d26')
        ),
        React.createElement('div', { className: 'nav-user' },
          React.createElement('button', { onClick: logout, className: 'logout-btn' }, '\\u9000\\u51fa\\u767b\\u5f55')
        )
      ),
      React.createElement('main', { className: 'page-content' },
        React.createElement('div', { className: 'profile-tabs' },
          React.createElement('button', { className: 'profile-tab' + (activeTab === 'profile' ? ' active' : ''), onClick: () => setActiveTab('profile') }, '\\u4e2a\\u4eba\\u8d44\\u6599'),
          React.createElement('button', { className: 'profile-tab' + (activeTab === 'family' ? ' active' : ''), onClick: () => setActiveTab('family') }, '\\u5bb6\\u5ead\\u7ba1\\u7406')
        ),
        activeTab === 'profile' ?
          React.createElement('div', { className: 'family-section' },
            message ? React.createElement('p', { style: { color: '#1f7a4d', marginBottom: '12px' } }, message) : null,
            React.createElement('div', { className: 'form-grid' },
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u59d3\\u540d'),
                React.createElement('input', { className: 'filter-input', value: formData.name, onChange: e => setFormData({...formData, name: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u6027\\u522b'),
                React.createElement('select', { className: 'filter-select', value: formData.gender, onChange: e => setFormData({...formData, gender: e.target.value}) },
                  React.createElement('option', { value: '' }, '\\u8bf7\\u9009\\u62e9'),
                  React.createElement('option', { value: '\\u7537' }, '\\u7537'),
                  React.createElement('option', { value: '\\u5973' }, '\\u5973')),
              ),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u5e74\\u9f84'),
                React.createElement('input', { className: 'filter-input', type: 'number', value: formData.age, onChange: e => setFormData({...formData, age: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u6708\\u5ea6\\u5de5\\u8d44(\\u5143)'),
                React.createElement('input', { className: 'filter-input', type: 'number', value: formData.monthSalary, onChange: e => setFormData({...formData, monthSalary: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u996e\\u98df\\u5fcc\\u53e3'),
                React.createElement('input', { className: 'filter-input', value: formData.dietTaboo, onChange: e => setFormData({...formData, dietTaboo: e.target.value}) })),
              React.createElement('div', { className: 'form-group' },
                React.createElement('label', null, '\\u559c\\u6b22\\u7684\\u83dc\\u54c1'),
                React.createElement('input', { className: 'filter-input', value: formData.tastePrefer, onChange: e => setFormData({...formData, tastePrefer: e.target.value}) }))
            ),
            React.createElement('button', { className: 'primary-button', onClick: handleSaveProfile, style: { marginTop: '16px', width: '100%' } }, '\\u4fdd\\u5b58\\u4fee\\u6539')
          ) :
          React.createElement('div', { className: 'family-section' },
            !inviteCode ?
              React.createElement('div', { style: { textAlign: 'center', padding: '20px' } },
                React.createElement('p', null, '\\u60a8\\u5c1a\\u672a\\u521b\\u5efa\\u5bb6\\u5ead\\u7ec4'),
                React.createElement('button', { className: 'primary-button', onClick: handleCreateFamily }, '\\u521b\\u5efa\\u5bb6\\u5ead')
              ) :
              React.createElement('div', null,
                React.createElement('div', { className: 'invite-code-box' },
                  React.createElement('span', null, '\\u9080\\u8bf7\\u7801:'),
                  React.createElement('span', { className: 'code' }, inviteCode),
                  React.createElement('button', { className: 'ghost-button small', onClick: () => { navigator.clipboard.writeText(inviteCode); alert('\\u5df2\\u590d\\u5236'); } }, '\\u590d\\u5236')
                ),
                React.createElement('div', { className: 'form-row', style: { marginBottom: '16px' } },
                  React.createElement('input', { className: 'filter-input', value: joinCode, onChange: e => setJoinCode(e.target.value), placeholder: '\\u8f93\\u5165\\u9080\\u8bf7\\u7801\\u52a0\\u5165\\u5bb6\\u5ead', style: { flex: 1 } }),
                  React.createElement('button', { className: 'primary-button', onClick: handleJoinFamily, disabled: !joinCode.trim() }, '\\u52a0\\u5165')
                ),
                React.createElement('div', { className: 'section-header', style: { marginBottom: '12px' } },
                  React.createElement('h3', { style: { margin: 0 } }, '\\u5bb6\\u5ead\\u6210\\u5458 (' + familyMembers.length + ')'),
                  React.createElement('button', { className: 'primary-button small', onClick: () => setShowAddMember(true) }, '\\u65b0\\u589e\\u6210\\u5458')
                ),
                showAddMember ? React.createElement('div', { className: 'modal-overlay', onClick: () => setShowAddMember(false) },
                  React.createElement('div', { className: 'modal-content', onClick: e => e.stopPropagation() },
                    React.createElement('h3', null, '\\u65b0\\u589e\\u5bb6\\u5ead\\u6210\\u5458'),
                    React.createElement('div', { className: 'form-group' },
                      React.createElement('label', null, '\\u59d3\\u540d'),
                      React.createElement('input', { className: 'filter-input', value: memberForm.name, onChange: e => setMemberForm({...memberForm, name: e.target.value}) })),
                    React.createElement('div', { className: 'form-row' },
                      React.createElement('div', { className: 'form-group' },
                        React.createElement('label', null, '\\u5e74\\u9f84'),
                        React.createElement('input', { className: 'filter-input', type: 'number', value: memberForm.age, onChange: e => setMemberForm({...memberForm, age: e.target.value}) })),
                      React.createElement('div', { className: 'form-group' },
                        React.createElement('label', null, '\\u4eba\\u7fa4\\u6807\\u7b7e'),
                        React.createElement('select', { className: 'filter-select', value: memberForm.personTag, onChange: e => setMemberForm({...memberForm, personTag: e.target.value}) },
                          React.createElement('option', { value: '\\u666e\\u901a' }, '\\u666e\\u901a'),
                          React.createElement('option', { value: '\\u513f\\u7ae5' }, '\\u513f\\u7ae5'),
                          React.createElement('option', { value: '\\u9752\\u5e74' }, '\\u9752\\u5e74'),
                          React.createElement('option', { value: '\\u8001\\u5e74' }, '\\u8001\\u5e74'),
                          React.createElement('option', { value: '\\u75c5\\u4eba' }, '\\u75c5\\u4eba'),
                          React.createElement('option', { value: '\\u51cf\\u80a5' }, '\\u51cf\\u80a5'))),
                    ),
                    React.createElement('div', { className: 'form-group' },
                      React.createElement('label', null, '\\u996e\\u98df\\u5fcc\\u53e3'),
                      React.createElement('input', { className: 'filter-input', value: memberForm.dietTaboo, onChange: e => setMemberForm({...memberForm, dietTaboo: e.target.value}) })),
                    React.createElement('div', { className: 'modal-actions' },
                      React.createElement('button', { className: 'ghost-button', onClick: () => setShowAddMember(false) }, '\\u53d6\\u6d88'),
                      React.createElement('button', { className: 'primary-button', onClick: handleAddMember }, '\\u786e\\u8ba4\\u6dfb\\u52a0'))
                  )
                ) : null,
                React.createElement('div', { className: 'member-list' },
                  familyMembers.map(m => React.createElement('div', { key: m.id, className: 'member-card' },
                    React.createElement('div', { className: 'member-info' },
                      React.createElement('strong', null, m.name),
                      React.createElement('span', null, m.personTag + ' | ' + (m.dietTaboo !== '\\u65e0' ? '\\u5fcc\\u53e3:' + m.dietTaboo : '\\u65e0\\u5fcc\\u53e3'))
                    ),
                    React.createElement('button', { className: 'ghost-button small danger', onClick: () => handleDeleteMember(m.id) }, '\\u5220\\u9664')
                  )),
                  familyMembers.length === 0 ? React.createElement('p', { style: { color: '#64716b', textAlign: 'center' } }, '\\u6682\\u65e0\\u5bb6\\u5ead\\u6210\\u5458') : null
                )
              )
          )
      )
    )
  );
}

'''

# Insert all pages after printMenu, before StockPage
content = content[:stock_start] + profile_page + content[stock_start:]

with open(r'D:\test\practice\frontend\src\main.jsx', 'w', encoding='utf-8') as f:
    f.write(content)
print('Added ProfilePage')
