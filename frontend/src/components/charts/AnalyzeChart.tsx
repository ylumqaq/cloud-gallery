import { useEffect, useRef } from 'react'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

// 通用图表 props
interface AnalyzeChartProps {
  option: EChartsOption // ECharts 配置项
  height?: number // 容器高度，默认 300
}

// 通用 ECharts 封装：负责实例创建 / 更新 / 自适应 / 销毁
export default function AnalyzeChart({ option, height = 300 }: AnalyzeChartProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<echarts.EChartsType | undefined>(undefined)

  useEffect(() => {
    if (!containerRef.current) return
    const chart = echarts.init(containerRef.current)
    chartRef.current = chart

    const handleResize = () => chart.resize()
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      chart.dispose()
      chartRef.current = undefined
    }
  }, [])

  useEffect(() => {
    chartRef.current?.setOption(option)
  }, [option])

  return <div ref={containerRef} style={{ width: '100%', height }} />
}
