import { useState } from 'react'
import { Input, Space, Button, ColorPicker } from 'antd'

// 图片搜索栏 props
interface PictureSearchBarProps {
  onSearch?: (params: { searchText?: string; picColor?: string }) => void
}

// 图片搜索栏：关键词 + 主色调筛选，回调父组件触发查询
export default function PictureSearchBar({ onSearch }: PictureSearchBarProps) {
  const [searchText, setSearchText] = useState('')
  const [picColor, setPicColor] = useState<string>()

  // 颜色选择回调：hex 形如 #ff0000，转为后端约定的 0xRRGGBB 格式
  function handleColorChange(_value: unknown, hex: string) {
    setPicColor(`0x${hex.replace('#', '')}`)
  }

  function handleSearch() {
    onSearch?.({
      searchText: searchText.trim() || undefined,
      picColor,
    })
  }

  return (
    <Space>
      <Input
        placeholder="搜索图片名称"
        value={searchText}
        onChange={(e) => setSearchText(e.target.value)}
        onPressEnter={handleSearch}
        allowClear
        style={{ width: 240 }}
      />
      <ColorPicker onChange={handleColorChange} />
      <Button type="primary" onClick={handleSearch}>
        搜索
      </Button>
    </Space>
  )
}
