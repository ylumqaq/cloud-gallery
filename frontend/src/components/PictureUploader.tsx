import { useState } from 'react'
import { Upload, Input, Button, Space, message, Radio } from 'antd'
import { InboxOutlined } from '@ant-design/icons'
import { uploadPictureApi } from '../api/picture'
import type { PictureVO } from '../types/picture'

// 允许的图片 MIME 类型
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
// 最大文件大小（字节）：2MB
const MAX_FILE_SIZE = 2 * 1024 * 1024

// 图片上传器 props
interface PictureUploaderProps {
  spaceId?: number // 目标空间，空 = 公共图库
  picName?: string // 图片名称（可选）
  category?: string // 图片分类（可选）
  tags?: string[] // 图片标签（可选，提交时转 JSON 字符串）
  onSuccess?: (picture: PictureVO) => void // 上传成功回调
}

// 图片上传器：支持本地文件 / 网络 URL 两种方式，前端做类型与大小预校验
export default function PictureUploader({ spaceId, picName, category, tags, onSuccess }: PictureUploaderProps) {
  const [mode, setMode] = useState<'file' | 'url'>('file')
  const [file, setFile] = useState<File | null>(null)
  const [fileUrl, setFileUrl] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // 提交上传：构造 FormData 并调用接口
  async function handleSubmit() {
    if (mode === 'file' && !file) {
      message.warning('请选择要上传的图片')
      return
    }
    if (mode === 'url' && !fileUrl.trim()) {
      message.warning('请输入图片 URL')
      return
    }

    const formData = new FormData()
    if (mode === 'file' && file) {
      formData.append('file', file)
    } else {
      formData.append('fileUrl', fileUrl.trim())
    }
    // 可选参数：spaceId 为空时不上传，后端按公共图库处理
    if (spaceId != null) formData.append('spaceId', String(spaceId))
    if (picName) formData.append('picName', picName)
    if (category) formData.append('category', category)
    if (tags && tags.length > 0) formData.append('tags', JSON.stringify(tags))

    setSubmitting(true)
    try {
      const picture = await uploadPictureApi(formData)
      message.success('上传成功')
      onSuccess?.(picture)
      // 上传成功后重置
      setFile(null)
      setFileUrl('')
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="large">
      <Radio.Group value={mode} onChange={(e) => setMode(e.target.value)}>
        <Radio.Button value="file">本地文件</Radio.Button>
        <Radio.Button value="url">网络 URL</Radio.Button>
      </Radio.Group>

      {mode === 'file' ? (
        <Upload.Dragger
          accept={ALLOWED_TYPES.join(',')}
          maxCount={1}
          beforeUpload={(f) => {
            // 类型校验
            if (!ALLOWED_TYPES.includes(f.type)) {
              message.error('仅支持 jpeg / png / webp 格式图片')
              return Upload.LIST_IGNORE
            }
            // 大小校验
            if (f.size > MAX_FILE_SIZE) {
              message.error('图片大小不能超过 2MB')
              return Upload.LIST_IGNORE
            }
            setFile(f)
            return false // 阻止自动上传，由提交按钮统一提交
          }}
          onRemove={() => setFile(null)}
        >
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽图片到此区域</p>
          <p className="ant-upload-hint">支持 jpeg / png / webp，大小不超过 2MB</p>
        </Upload.Dragger>
      ) : (
        <Input
          placeholder="请输入图片 URL"
          value={fileUrl}
          onChange={(e) => setFileUrl(e.target.value)}
        />
      )}

      <Button type="primary" loading={submitting} onClick={handleSubmit}>
        上传
      </Button>
    </Space>
  )
}
