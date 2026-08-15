import AnalyzeChart from './AnalyzeChart'
import type { EChartsOption } from 'echarts'
import type { SpaceCategoryAnalyzeVO } from '../../types/spaceAnalyze'

// 分类分析柱状图
export default function CategoryAnalyzeChart({ data }: { data: SpaceCategoryAnalyzeVO[] }) {
  const option: EChartsOption = {
    title: { text: '分类分析' },
    tooltip: {},
    xAxis: { type: 'category', data: data.map((d) => d.category || '未分类') },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map((d) => d.count) }],
  }
  return <AnalyzeChart option={option} />
}
