const defaultMenus = [
  { id: 1, menuName: '首页', path: '/admin/dashboard', icon: 'HomeFilled', children: [] },
  {
    id: 2, menuName: '用户管理', path: '/admin/user', icon: 'User',
    children: [
      { id: 21, menuName: '用户列表', path: '/admin/user/list' },
      { id: 22, menuName: '个人中心', path: '/admin/user/profile' }
    ]
  },
  {
    id: 3, menuName: '食材管理', path: '/admin/food', icon: 'Apple',
    children: [
      { id: 31, menuName: '食材分类', path: '/admin/food/category' },
      { id: 32, menuName: '食材信息', path: '/admin/food/list' },
      { id: 33, menuName: '食谱管理', path: '/admin/food/recipe' }
    ]
  },
  {
    id: 4, menuName: '饮食推荐', path: '/admin/recommend', icon: 'MagicStick',
    children: [
      { id: 41, menuName: '每日推荐', path: '/admin/recommend/daily' },
      { id: 42, menuName: 'AI推荐', path: '/admin/recommend/ai' },
      { id: 43, menuName: '推荐规则', path: '/admin/recommend/rule' },
      { id: 44, menuName: '推荐记录', path: '/admin/recommend/record' }
    ]
  },
  { id: 5, menuName: '饮食清单', path: '/admin/diet-plan/list', icon: 'Document', children: [] },
  { id: 6, menuName: '健康档案', path: '/admin/health/list', icon: 'FirstAidKit', children: [] },
  { id: 8, menuName: '系统公告', path: '/admin/notice/list', icon: 'Bell', children: [] }
]

export default defaultMenus
