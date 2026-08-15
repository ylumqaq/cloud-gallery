import { Card, Tag, Typography, Space } from 'antd'
import { EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { PictureVO } from '../types/picture'

const { Text } = Typography

// 图片卡片 props
interface PictureCardProps {
  picture: PictureVO // 图片数据（tags 已解析为 string[]）
  showActions?: boolean // 是否展示操作按钮，默认 false
  onPreview?: (picture: PictureVO) => void
  onEdit?: (picture: PictureVO) => void
  onDelete?: (picture: PictureVO) => void
}

// 将字节大小格式化为 B / KB / MB
function formatSize(size?: number): string {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

// 将后端主色调 0xRRGGBB 转为 CSS 颜色 #RRGGBB
function toCssColor(picColor?: string): string | undefined {
  if (!picColor) return undefined
  return `#${picColor.replace(/^0x/i, '')}`
}

// 图片卡片：展示缩略图、元信息与操作入口（编辑 / 删除由调用方按权限控制）
export default function PictureCard({ picture, showActions = false, onPreview, onEdit, onDelete }: PictureCardProps) {
  const actions = []
  if (onPreview) actions.push(<EyeOutlined key="preview" onClick={() => onPreview(picture)} />)
  if (showActions && onEdit) actions.push(<EditOutlined key="edit" onClick={() => onEdit(picture)} />)
  if (showActions && onDelete) actions.push(<DeleteOutlined key="delete" onClick={() => onDelete(picture)} />)

  return (
    <Card
      hoverable
      cover={
        <div
          style={{
            height: 160,
            overflow: 'hidden',
            background: '#f5f5f5',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <img
            src={picture.thumbnailUrl || picture.url}
            alt={picture.name}
            style={{ maxWidth: '100%', maxHeight: 160, objectFit: 'cover', cursor: 'pointer' }}
            onClick={() => onPreview?.(picture)}
          />
        </div>
      }
      actions={actions.length > 0 ? actions : undefined}
    >
      <Card.Meta
        title={<Text ellipsis>{picture.name}</Text>}
        description={
          <Space direction="vertical" size={4} style={{ width: '100%' }}>
            {picture.category && <Text type="secondary">分类：{picture.category}</Text>}
            <Space size={4} wrap>
              {picture.tags?.map((tag) => (
                <Tag key={tag} color="blue">
                  {tag}
                </Tag>
              ))}
            </Space>
            <Space size={8}>
              <Text type="secondary">大小：{formatSize(picture.picSize)}</Text>
              {picture.picColor && (
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                  <span
                    style={{
                      width: 12,
                      height: 12,
                      borderRadius: 2,
                      background: toCssColor(picture.picColor),
                      display: 'inline-block',
                    }}
                  />
                  <Text type="secondary">{picture.picColor}</Text>
                </span>
              )}
            </Space>
          </Space>
        }
      />
    </Card>
  )
}
