export const JOB_CATEGORY_OPTIONS = [
  {
    value: '互联网/AI',
    label: '互联网/AI',
    children: [
      { value: '互联网', label: '互联网' },
      { value: '电子商务', label: '电子商务' },
      { value: '计算机软件', label: '计算机软件' },
      { value: '生活服务(O2O)', label: '生活服务(O2O)' },
      { value: '企业服务', label: '企业服务' },
      { value: '医疗健康', label: '医疗健康' },
      { value: '游戏', label: '游戏' },
      { value: '社交网络与媒体', label: '社交网络与媒体' },
      { value: '人工智能', label: '人工智能' },
      { value: '云计算', label: '云计算' },
      { value: '在线教育', label: '在线教育' },
      { value: '计算机服务', label: '计算机服务' },
      { value: '新零售', label: '新零售' },
      { value: '大数据', label: '大数据' },
      { value: '广告营销', label: '广告营销' },
      { value: '物联网', label: '物联网' },
      { value: '信息安全', label: '信息安全' }
    ]
  },
  {
    value: '电子/通信/半导体',
    label: '电子/通信/半导体',
    children: [
      { value: '半导体/芯片', label: '半导体/芯片' },
      { value: '电子/硬件开发', label: '电子/硬件开发' },
      { value: '通信/网络设备', label: '通信/网络设备' },
      { value: '智能硬件/消费电子', label: '智能硬件/消费电子' },
      { value: '运营商/增值服务', label: '运营商/增值服务' },
      { value: '计算机硬件', label: '计算机硬件' }
    ]
  },
  {
    value: '金融',
    label: '金融',
    children: [
      { value: '银行', label: '银行' },
      { value: '证券', label: '证券' },
      { value: '保险', label: '保险' },
      { value: '基金', label: '基金' },
      { value: '信托', label: '信托' },
      { value: '互联网金融', label: '互联网金融' },
      { value: '投资', label: '投资' }
    ]
  },
  {
    value: '房地产/建筑',
    label: '房地产/建筑',
    children: [
      { value: '房地产开发', label: '房地产开发' },
      { value: '物业管理', label: '物业管理' },
      { value: '建筑设计', label: '建筑设计' },
      { value: '工程施工', label: '工程施工' },
      { value: '装饰装修', label: '装饰装修' },
      { value: '建材', label: '建材' }
    ]
  },
  {
    value: '汽车/机械/制造',
    label: '汽车/机械/制造',
    children: [
      { value: '汽车研发', label: '汽车研发' },
      { value: '汽车制造', label: '汽车制造' },
      { value: '汽车零部件', label: '汽车零部件' },
      { value: '机械设计', label: '机械设计' },
      { value: '机械制造', label: '机械制造' },
      { value: '自动化设备', label: '自动化设备' }
    ]
  },
  {
    value: '消费品/零售',
    label: '消费品/零售',
    children: [
      { value: '快消品', label: '快消品' },
      { value: '零售', label: '零售' },
      { value: '服装纺织', label: '服装纺织' },
      { value: '食品饮料', label: '食品饮料' },
      { value: '化妆品', label: '化妆品' },
      { value: '家居用品', label: '家居用品' }
    ]
  },
  {
    value: '教育/培训',
    label: '教育/培训',
    children: [
      { value: 'K12教育', label: 'K12教育' },
      { value: '高等教育', label: '高等教育' },
      { value: '职业培训', label: '职业培训' },
      { value: '语言培训', label: '语言培训' },
      { value: '素质教育', label: '素质教育' }
    ]
  },
  {
    value: '物流/交通/贸易',
    label: '物流/交通/贸易',
    children: [
      { value: '物流', label: '物流' },
      { value: '快递', label: '快递' },
      { value: '仓储', label: '仓储' },
      { value: '航运', label: '航运' },
      { value: '航空', label: '航空' },
      { value: '国际贸易', label: '国际贸易' },
      { value: '进出口', label: '进出口' }
    ]
  },
  {
    value: '能源/化工/环保',
    label: '能源/化工/环保',
    children: [
      { value: '石油石化', label: '石油石化' },
      { value: '新能源', label: '新能源' },
      { value: '电力', label: '电力' },
      { value: '化工', label: '化工' },
      { value: '环保', label: '环保' },
      { value: '新材料', label: '新材料' }
    ]
  },
  {
    value: '专业服务',
    label: '专业服务',
    children: [
      { value: '会计审计', label: '会计审计' },
      { value: '法律咨询', label: '法律咨询' },
      { value: '人力资源', label: '人力资源' },
      { value: '管理咨询', label: '管理咨询' },
      { value: '广告公关', label: '广告公关' },
      { value: '检测认证', label: '检测认证' }
    ]
  },
  {
    value: '政府/非营利组织',
    label: '政府/非营利组织',
    children: [
      { value: '政府机关', label: '政府机关' },
      { value: '事业单位', label: '事业单位' },
      { value: '科研机构', label: '科研机构' },
      { value: '社会组织', label: '社会组织' },
      { value: '公益慈善', label: '公益慈善' }
    ]
  }
]

export const JOB_CATEGORY_FLAT_OPTIONS = JOB_CATEGORY_OPTIONS.flatMap((item) => {
  if (!item.children || item.children.length === 0) {
    return []
  }

  return item.children.map((child) => ({
    value: child.value,
    label: child.label
  }))
})

export const resolveCategoryFilterValue = (value) => {
  if (value == null) {
    return ''
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return ''
    }
    const last = value[value.length - 1]
    return typeof last === 'string' ? last.trim() : String(last || '').trim()
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) {
      return ''
    }

    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (Array.isArray(parsed)) {
          return parsed.length > 0 ? String(parsed[parsed.length - 1] || '').trim() : ''
        }
      } catch (error) {
        return trimmed
      }
    }

    return trimmed
  }

  return String(value).trim()
}
