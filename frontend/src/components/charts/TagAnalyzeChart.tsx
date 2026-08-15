import AnalyzeChart from './AnalyzeChart'
import type { EChartsOption } from 'echarts'
import type { SpaceTagAnalyzeVO } from '../../types/spaceAnalyze'

// 标签分析柱状图
export default function TagAnalyzeChart({ data }: { data: SpaceTagAnalyzeVO[] }) {
  const option: EChartsOption = {
    title: { text: '标签分析' },
    tooltip: {},
    xAxis: { type: 'category', data: data.map((d) => d.tag) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map((d) => d.count) }],
  }
  return <AnalyzeChart option={option} />
}
