import { useCallback, useEffect, useState } from 'react'
import { Row, Col, Pagination, Empty, Spin, Space, Modal, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import PictureSearchBar from '../components/PictureSearchBar'
import PictureCard from '../components/PictureCard'
import { PERMISSION } from '../constants/permission'
import { hasPermission } from '../utils/permission'
import { deletePictureApi, listPictureByPageApi } from '../api/picture'
import { useSpaceStore } from '../stores/space'
import type { PictureVO } from '../types/picture'

// 图片列表页：公共图库（/）与空间图片（/space/:spaceId）复用，差异仅在 spaceId 与权限来源
export default function PictureListPage() {
  const currentSpace = useSpaceStore((s) => s.currentSpace)
  const navigate = useNavigate()

  const [pictureList, setPictureList] = useState<PictureVO[]>([])
  const [loading, setLoading] = useState(false)
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [total, setTotal] = useState(0)
  const [searchText, setSearchText] = useState<string>()
  const [picColor, setPicColor] = useState<string>()

  // 拉取图片列表
  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const res = await listPictureByPageApi({
        current,
        pageSize,
        spaceId: currentSpace?.id, // 公共图库为 undefined
        searchText,
        picColor,
      })
      setPictureList(res.records)
      setTotal(res.total)
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setLoading(false)
    }
  }, [current, pageSize, currentSpace, searchText, picColor])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  // 搜索：重置到第一页
  function handleSearch(params: { searchText?: string; picColor?: string }) {
    setSearchText(params.searchText)
    setPicColor(params.picColor)
    setCurrent(1)
  }

  // 删除图片（二次确认）
  function handleDelete(picture: PictureVO) {
    Modal.confirm({
      title: '删除图片',
      content: `确定删除「${picture.name}」吗？删除后不可恢复。`,
      onOk: async () => {
        await deletePictureApi(picture.id)
        message.success('删除成功')
        // 当前页仅剩一条时回退上一页
        if (pictureList.length === 1 && current > 1) {
          setCurrent(current - 1)
        } else {
          fetchList()
        }
      },
    })
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <PictureSearchBar onSearch={handleSearch} />

      <Spin spinning={loading}>
        {pictureList.length === 0 ? (
          <Empty description="暂无图片" />
        ) : (
          <Row gutter={[16, 16]}>
            {pictureList.map((picture) => (
              <Col key={picture.id} xs={24} sm={12} md={8} lg={6}>
                <PictureCard
                  picture={picture}
                  showActions
                  onPreview={(p) => navigate(`/picture/${p.id}`)}
                  onEdit={hasPermission(PERMISSION.PICTURE_EDIT) ? (p) => navigate(`/picture/${p.id}`) : undefined}
                  onDelete={hasPermission(PERMISSION.PICTURE_DELETE) ? handleDelete : undefined}
                />
              </Col>
            ))}
          </Row>
        )}
      </Spin>

      {total > 0 && (
        <Pagination
          current={current}
          pageSize={pageSize}
          total={total}
          onChange={(page, size) => {
            setCurrent(page)
            setPageSize(size)
          }}
          showSizeChanger
        />
      )}
    </Space>
  )
}
