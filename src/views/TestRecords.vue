<template>
  <div class="test-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>测试记录</h2>
        </div>
      </template>

      <el-table 
        :data="recordsList"
        :highlight-current-row="true"
        ref="recordsTableRef"
      >
        <el-table-column prop="name" label="测试名称" />
        <el-table-column prop="date" label="完成时间">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分">
          <template #default="{ row }">
            <el-tag 
              :type="getScoreType(row.score)"
            >
              {{ row.score }}分
            </el-tag>
            <el-tooltip
              v-if="row.score < 60"
              effect="dark"
              content="建议您及时寻求专业帮助"
              placement="top"
            >
              <el-icon class="warning-icon"><Warning /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button
              type="success"
              link
              @click="handleDownloadReport(row)"
            >
              下载报告
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDeleteRecord(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
        v-model="detailVisible"
        title="测试详情"
        width="50%"
      >
        <div v-if="currentRecord" class="test-detail">
          <h3>{{ currentRecord.title }}</h3>
          <p class="complete-time">完成时间：{{ formatDate(currentRecord.completedAt) }}</p>
          <div class="score-info">
            <span>得分：</span>
            <el-tag :type="getScoreType(currentRecord.score)" size="large">
              {{ currentRecord.score }}分
            </el-tag>
          </div>
          <div class="result-content">
            <p class="description">{{ currentRecord.resultDescription }}</p>
            <div class="suggestions">
              <h4>建议：</h4>
              <ul>
                <li v-for="(suggestion, index) in currentRecord.suggestions" :key="index">
                  {{ suggestion }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Warning, Download } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTestRecordsStore } from '@/stores/testRecords'

const route = useRoute()
const detailVisible = ref(false)
const currentRecord = ref(null)
const loading = ref(false)
const recordsTableRef = ref(null)
const testRecordsStore = useTestRecordsStore()

const getScoreType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 75) return 'warning'
  if (score >= 60) return 'info'
  return 'danger'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleViewDetail = (record) => {
  currentRecord.value = record
  detailVisible.value = true
}

const handleDownloadReport = async (record) => {
  try {
    loading.value = true
    // 这里应该调用后端API获取报告数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 生成报告内容
    const reportContent = generateReportContent(record)
    
    // 创建并下载文件
    const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `心理测试报告_${record.name}_${formatDate(record.date)}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('报告下载成功')
  } catch (error) {
    ElMessage.error('报告下载失败')
  } finally {
    loading.value = false
  }
}

// 生成报告内容
const generateReportContent = (record) => {
  return `
心理测试报告
===============

测试名称：${record.name}
完成时间：${formatDate(record.date)}
测试评级：${record.grade}

测试结果分析
---------------
${record.resultDescription || '暂无详细分析'}

建议措施
---------------
${record.suggestions ? record.suggestions.join('\n') : '暂无具体建议'}

注意事项
---------------
1. 本报告仅供参考，不作为医疗诊断依据
2. 如有严重心理问题，请及时就医
3. 保持积极乐观的心态，培养健康的生活方式
`
}

const handleDeleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条测试记录吗？删除后将无法恢复',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 使用 store 的 deleteTest 方法来同时删除测试和记录
    testRecordsStore.deleteTest(record.id)
    // 强制更新视图
    recordsList.value = recordsList.value.filter(r => r.id !== record.id)
    ElMessage.success('删除成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const recordsList = computed(() => testRecordsStore.records)

onMounted(() => {
  if (route.query.testId && route.query.highlight) {
    const index = recordsList.value.findIndex(
      record => record.id === Number(route.query.testId)
    )
    if (index !== -1) {
      recordsTableRef.value?.setCurrentRow(recordsList.value[index])
    }
  }
})
</script>

<style scoped>
.warning-icon {
  color: #F56C6C;
  margin-left: 5px;
  cursor: help;
}

.el-tag {
  min-width: 60px;
  text-align: center;
}
</style> 