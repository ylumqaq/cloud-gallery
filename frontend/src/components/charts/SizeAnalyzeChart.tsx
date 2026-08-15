import AnalyzeChart from './AnalyzeChart'
import type { EChartsOption } from 'echarts'
import type { SpaceSizeAnalyzeVO } from '../../types/spaceAnalyze'

// 大小区间分布柱状图
export default function SizeAnalyzeChart({ data }: { data: SpaceSizeAnalyzeVO[] }) {
  const option: EChartsOption = {
    title: { text: '大小分布' },
    tooltip: {},
    xAxis: { type: 'category', data: data.map((d) => d.sizeRange) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map((d) => d.count) }],
  }
  return <AnalyzeChart option={option} />
}
