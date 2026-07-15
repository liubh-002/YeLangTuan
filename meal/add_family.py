with open(r'D:\test\practice\frontend\src\main.jsx', 'r', encoding='utf-8') as f:
    content = f.read()

# Find where to insert remaining pages - after ProfilePage, before StockPage
pp_idx = content.find('function ProfilePage()')
sp_idx = content.find('function StockPage()', pp_idx)
print(f"ProfilePage at {pp_idx}, StockPage at {sp_idx}")

# Add FamilyMembersPage (a simple wrapper that redirects or shows member list)
family_page = '''

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

  if (loading) return React.createElement('div', { className: 'app-container' }, React.createElement('div', { className: 'loading-screen' }, React.createElement('p', null, '\\u52a0\\u8f7d\\u4e2d...')));

  return React.createElement('div', { className: 'app-container' },
    React.createElement(TopNav, null,
      React.createElement('div', { className: 'nav-links' },
        React.createElement('button', { onClick: () => navigate('/'), className: 'nav-link' }, '\\u9996\\u9875'),
        React.createElement('button', { onClick: () => navigate('/profile'), className: 'nav-link' }, '\\u4e2a\\u4eba\\u4e2d\\u5fc3')
      )
    ),
    React.createElement('main', { className: 'page-content' },
      React.createElement('h2', null, '\\u5bb6\\u5ead\\u6210\\u5458'),
      members.length === 0 ?
        React.createElement('div', { className: 'empty-state' }, React.createElement('h2', null, '\\u6682\\u65e0\\u5bb6\\u5ead\\u6210\\u5458')) :
        React.createElement('div', { className: 'member-list' },
          members.map(m => React.createElement('div', { key: m.id, className: 'member-card' },
            React.createElement('div', { className: 'member-info' },
              React.createElement('strong', null, m.name),
              React.createElement('span', null, m.personTag + ' | \\u5e74\\u9f84:' + m.age)
            )
          ))
        )
    )
  );
}

'''

# Insert before StockPage
content = content[:sp_idx] + family_page + content[sp_idx:]

with open(r'D:\test\practice\frontend\src\main.jsx', 'w', encoding='utf-8') as f:
    f.write(content)
print('Added FamilyMembersPage')
