<template>
  <div class="psychological-test">
    <!-- 测试列表卡片 -->
    <el-card v-if="!currentTest" class="test-list-card">
      <template #header>
        <div class="card-header">
          <h2>心理测试</h2>
          <el-button type="primary" @click="handleStartTest" :disabled="hasUnfinishedTest">
            开始新测试
          </el-button>
        </div>
      </template>
      
      <!-- 未完成的测试提醒 -->
      <el-alert
        v-if="hasUnfinishedTest"
        type="warning"
        :closable="false"
        show-icon
      >
        您有未完成的测试，请继续完成或保存后再开始新的测试
      </el-alert>

      <!-- 测试列表 -->
      <div class="test-list">
        <el-table :data="testList">
          <el-table-column prop="title" label="测试名称" />
          <el-table-column prop="createdAt" label="创建时间">
            <template #default="{ row }">
              {{ new Date(row.createdAt).toLocaleString('zh-CN') }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'completed' ? 'success' : 'info'">
                {{ row.status === 'completed' ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button 
                v-if="row.status !== 'completed'"
                @click="continueTest(row)"
                type="primary"
                size="small"
              >
                继续测试
              </el-button>
              <el-button 
                v-if="row.status === 'completed'"
                @click="viewResult(row)"
                type="success"
                size="small"
              >
                查看结果
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDeleteTest(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 测试问卷 -->
    <el-card v-else class="test-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button 
              @click="handleReturn" 
              text
            >
              <el-icon class="back-icon"><ArrowLeft /></el-icon>
            </el-button>
            <h2>{{ currentTest.title }}</h2>
            <el-tag>题目 {{ currentQuestionIndex + 1 }}/{{ currentTest.questions.length }}</el-tag>
          </div>
          <div class="header-right">
            <el-button @click="handleSaveTest">
              保存进度
            </el-button>
            <el-button type="primary" @click="handleSubmitTest" :disabled="!canSubmit">
              提交测试
            </el-button>
          </div>
        </div>
      </template>

      <!-- 进度条 -->
      <div class="progress-info">
        已完成 {{ answeredCount }}/{{ totalQuestions }} 题
      </div>

      <!-- 问题区域 -->
      <div class="question-area" v-loading="loading">
        <div class="question-content">
          <h3>{{ currentQuestion.question }}</h3>
          <p class="question-desc" v-if="currentQuestion.description">
            {{ currentQuestion.description }}
          </p>
        </div>

        <!-- 选项 -->
        <el-radio-group 
          v-model="answers[currentQuestionIndex]"
          class="answer-options"
        >
          <el-radio 
            v-for="option in currentQuestion.options" 
            :key="option.value"
            :label="option.value"
            border
          >
            {{ option.label }}
          </el-radio>
        </el-radio-group>

        <!-- 导航按钮 -->
        <div class="question-nav">
          <el-button 
            :disabled="currentQuestionIndex === 0"
            @click="handlePrevQuestion"
          >
            上一题
          </el-button>
          <el-button 
            type="primary"
            :disabled="currentQuestionIndex === currentTest.questions.length - 1"
            @click="handleNextQuestion"
          >
            下一题
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 结果对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="测试结果"
      width="60%"
    >
      <div class="test-result" v-if="selectedTest">
        <div class="result-header">
          <h3>{{ selectedTest.title }}</h3>
          <el-tag :type="getGradeType(selectedTest.grade)" size="large">
            评级: {{ selectedTest.grade }}
          </el-tag>
        </div>
        <div class="result-content">
          <p class="result-desc">{{ selectedTest.resultDescription }}</p>
          <div class="result-suggestions">
            <h4>建议：</h4>
            <ul>
              <li v-for="(suggestion, index) in selectedTest.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 选择测试类型对话框 -->
    <el-dialog
      v-model="showTestTypeDialog"
      title="选择测试类型"
      width="400px"
      align-center
    >
      <div class="test-type-options">
        <el-radio-group v-model="selectedTestType" class="radio-group">
          <el-radio 
            v-for="type in testTypes" 
            :key="type.value" 
            :label="type.value"
            class="radio-item"
          >
            {{ type.label }}
          </el-radio>
        </el-radio-group>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showTestTypeDialog = false">取消</el-button>
          <el-button type="primary" @click="handleTestTypeConfirm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useTestRecordsStore } from '@/stores/testRecords'
import { storeToRefs } from 'pinia'

const router = useRouter()
const testStore = useTestRecordsStore()
const { tests } = storeToRefs(testStore)

// 模拟测试数据
const mockTest = {
  id: 1,
  title: '心理健康评估问卷',
  questions: [
    {
      question: '您最近的睡眠质量如何？',
      description: '请根据过去一周的整体感受来选择',
      options: [
        { label: '很好', value: 'A' },
        { label: '一般', value: 'B' },
        { label: '较差', value: 'C' },
        { label: '很差', value: 'D' }
      ]
    },
    // ... 更多问题
  ]
}

const loading = ref(false)
const currentTest = ref(null)
const currentQuestionIndex = ref(0)
const answers = ref([])
const dialogVisible = ref(false)
const selectedTest = ref(null)
const testList = tests

// 计算属性
const currentQuestion = computed(() => 
  currentTest.value?.questions[currentQuestionIndex.value] || {}
)

// 计算总题目数和已答题数
const totalQuestions = computed(() => 
  currentTest.value?.questions.length || 0
)

const answeredCount = computed(() => 
  answers.value.filter(answer => answer !== undefined).length
)

const canSubmit = computed(() => 
  answers.value.length === currentTest.value?.questions.length &&
  !answers.value.includes(undefined)
)

const hasUnfinishedTest = computed(() => 
  testList.value.some(test => test.status === 'unfinished')
)

// 方法
const getStatusType = (status) => {
  const types = {
    unfinished: 'warning',
    grading: 'info',
    completed: 'success'
  }
  return types[status]
}

const getStatusText = (status) => {
  const texts = {
    unfinished: '未完成',
    grading: '评级中',
    completed: '已完成'
  }
  return texts[status]
}

const getGradeType = (grade) => {
  const types = {
    'A': 'success',
    'B': 'warning',
    'C': 'danger'
  }
  return types[grade]
}

const handleStartTest = () => {
  showTestTypeDialog.value = true
}

const continueTest = (row) => {
  // 根据测试类型获取对应的测试问卷
  const testTemplate = availableTests.find(test => test.id === row.testType)
  if (!testTemplate) {
    ElMessage.error('未找到测试问卷')
    return
  }
  
  // 设置当前测试
  currentTest.value = {
    ...testTemplate,
    id: row.id,
    title: row.title,
    savedAnswers: row.answers || [] // 如果有保存的答案则加载
  }
  
  // 恢复已保存的答案
  answers.value = currentTest.value.savedAnswers || []
  
  // 修改这里：找到第一个未回答的题目索引
  const firstUnansweredIndex = answers.value.findIndex(answer => answer === undefined)
  currentQuestionIndex.value = firstUnansweredIndex !== -1 ? firstUnansweredIndex : 0
}

const viewResult = (row) => {
  // 直接跳转到测试记录界面并定位到对应记录
  router.push({
    path: '/test-records',
    query: { 
      testId: row.id,
      highlight: true // 用于高亮显示对应记录
    }
  })
}

const handlePrevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const handleNextQuestion = () => {
  if (currentQuestionIndex.value < currentTest.value.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

const handleSaveTest = async () => {
  try {
    // 这里应该调用后端API保存进度
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('进度保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 计算评级的函数
const calculateGrade = (answers) => {
  const score = answers.reduce((total, answer) => {
    const scores = { 'A': 4, 'B': 3, 'C': 2, 'D': 1 }
    return total + scores[answer]
  }, 0)
  const average = score / answers.length
  if (average >= 3.5) return 'A'
  if (average >= 2.5) return 'B'
  return 'C'
}

const handleSubmitTest = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交测试吗？提交后将无法修改答案',
      '提交确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    
    const score = calculateScore(answers.value, currentTest.value.questions)
    
    // 更新测试状态和结果
    const testIndex = testList.value.findIndex(test => test.id === currentTest.value.id)
    if (testIndex !== -1) {
      const testResult = {
        ...testList.value[testIndex],
        status: 'completed',
        score,
        answers: [...answers.value],
        completedAt: new Date().toISOString(),
        resultDescription: generateResultDescription(score),
        suggestions: generateSuggestions(score)
      }
      
      // 更新测试列表
      testStore.updateTest(currentTest.value.id, testResult)
      
      // 添加到测试记录
      testStore.addRecord({
        id: currentTest.value.id,
        name: currentTest.value.title,
        date: new Date().toISOString(),
        score,
        resultDescription: testResult.resultDescription,
        suggestions: testResult.suggestions,
        completedAt: testResult.completedAt
      })
    }
    
    ElMessage.success('测试提交成功')
    currentTest.value = null
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  } finally {
    loading.value = false
  }
}

const showTestTypeDialog = ref(false)
const selectedTestType = ref('')

// 可选的测试类型
const availableTests = [
  {
    id: 'anxiety',
    title: '焦虑自评量表',
    questions: [
      {
        question: '您最近的睡眠质量如何？',
        description: '请根据过去一周的整体感受来选择',
        options: [
          { label: '很好', value: 'A', score: 20 },
          { label: '一般', value: 'B', score: 15 },
          { label: '较差', value: 'C', score: 10 },
          { label: '很差', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否经常感到紧张或焦虑？',
        description: '考虑您在日常生活中的整体感受',
        options: [
          { label: '从不', value: 'A', score: 20 },
          { label: '偶尔', value: 'B', score: 15 },
          { label: '经常', value: 'C', score: 10 },
          { label: '总是', value: 'D', score: 5 }
        ]
      },
      {
        question: '当面对压力时您是否容易出现身体不适？',
        options: [
          { label: '完全不会', value: 'A', score: 20 },
          { label: '较少出现', value: 'B', score: 15 },
          { label: '时常出现', value: 'C', score: 10 },
          { label: '频繁出现', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否经常感到心跳加快或呼吸困难？',
        options: [
          { label: '从不', value: 'A', score: 20 },
          { label: '偶尔', value: 'B', score: 15 },
          { label: '经常', value: 'C', score: 10 },
          { label: '总是', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否能够保持注意力集中？',
        options: [
          { label: '总是能', value: 'A', score: 20 },
          { label: '经常能', value: 'B', score: 15 },
          { label: '偶尔能', value: 'C', score: 10 },
          { label: '很难集中', value: 'D', score: 5 }
        ]
      }
    ]
  },
  {
    id: 'depression',
    title: '抑郁自评量表',
    questions: [
      {
        question: '您是否对日常活动失去兴趣？',
        description: '包括之前喜欢的活动或爱好',
        options: [
          { label: '没有变化', value: 'A', score: 20 },
          { label: '略有减少', value: 'B', score: 15 },
          { label: '明显减少', value: 'C', score: 10 },
          { label: '完全失去兴趣', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否经常感到情绪低落或沮丧？',
        options: [
          { label: '从不', value: 'A', score: 20 },
          { label: '偶尔', value: 'B', score: 15 },
          { label: '经常', value: 'C', score: 10 },
          { label: '总是', value: 'D', score: 5 }
        ]
      },
      {
        question: '您对未来的想法如何？',
        options: [
          { label: '充满希望', value: 'A', score: 20 },
          { label: '一般', value: 'B', score: 15 },
          { label: '有些消极', value: 'C', score: 10 },
          { label: '非常悲观', value: 'D', score: 5 }
        ]
      },
      {
        question: '您的睡眠质量如何？',
        options: [
          { label: '很好', value: 'A', score: 20 },
          { label: '一般', value: 'B', score: 15 },
          { label: '较差', value: 'C', score: 10 },
          { label: '很差', value: 'D', score: 5 }
        ]
      },
      {
        question: '您的食欲状况如何？',
        options: [
          { label: '正常', value: 'A', score: 20 },
          { label: '略有减退', value: 'B', score: 15 },
          { label: '明显减退', value: 'C', score: 10 },
          { label: '几乎没有食欲', value: 'D', score: 5 }
        ]
      }
    ]
  },
  {
    id: 'pressure',
    title: '压力评估量表',
    questions: [
      {
        question: '您的工作/学习压力程度如何？',
        description: '考虑最近一个月的整体感受',
        options: [
          { label: '压力很小', value: 'A', score: 20 },
          { label: '压力适中', value: 'B', score: 15 },
          { label: '压力较大', value: 'C', score: 10 },
          { label: '压力非常大', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否经常感到时间不够用？',
        options: [
          { label: '时间充裕', value: 'A', score: 20 },
          { label: '偶尔紧张', value: 'B', score: 15 },
          { label: '经常紧张', value: 'C', score: 10 },
          { label: '总是不充裕', value: 'D', score: 5 }
        ]
      },
      {
        question: '您的人际关系是否给您带来压力？',
        options: [
          { label: '完全没有', value: 'A', score: 20 },
          { label: '偶尔有', value: 'B', score: 15 },
          { label: '经常有', value: 'C', score: 10 },
          { label: '压力很大', value: 'D', score: 5 }
        ]
      },
      {
        question: '您的经济状况是否让您感到压力？',
        options: [
          { label: '没有压力', value: 'A', score: 20 },
          { label: '轻微压力', value: 'B', score: 15 },
          { label: '中等压力', value: 'C', score: 10 },
          { label: '重度压力', value: 'D', score: 5 }
        ]
      },
      {
        question: '您是否能够保持工作与生活的平衡？',
        options: [
          { label: '总是能', value: 'A', score: 20 },
          { label: '经常能', value: 'B', score: 15 },
          { label: '偶尔能', value: 'C', score: 10 },
          { label: '很难平衡', value: 'D', score: 5 }
        ]
      }
    ]
  }
]

// 在 script setup 中添加测试类型数据
const testTypes = [
  {
    label: '焦虑自评量表',
    value: 'anxiety'
  },
  {
    label: '抑郁自评量表',
    value: 'depression'
  },
  {
    label: '压力评估量表',
    value: 'pressure'
  }
]

// 修改 handleTestTypeConfirm 函数
const handleTestTypeConfirm = () => {
  const selectedTest = availableTests.find(test => test.id === selectedTestType.value)
  if (selectedTest) {
    // 检查是否已存在相同称的测试
    const existingTest = testList.value.find(
      test => test.title === selectedTest.title && test.status !== 'completed'
    )
    
    if (existingTest) {
      ElMessage.warning('已存在未完成的相同测试，请先完成当前测试')
      showTestTypeDialog.value = false
      selectedTestType.value = ''
      return
    }
    
    // 添加到 store 中
    testStore.addTest({
      id: Date.now(),
      title: selectedTest.title,
      status: 'incomplete',
      testType: selectedTest.id,
      answers: new Array(selectedTest.questions.length).fill(undefined),
      date: new Date().toISOString().split('T')[0],
      createdAt: new Date(),
    })
    
    showTestTypeDialog.value = false
    selectedTestType.value = ''
    ElMessage.success('测试创建成功')
  }
}

// 删除测试
const handleDeleteTest = async (test) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个测试吗？删除后将无法恢复',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 使用 store 的 deleteTest 方法来同时删除测试和记录
    testStore.deleteTest(test.id)
    ElMessage.success('删除成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleReturn = () => {
  currentTest.value = null
}

// 修改计算分数的函数
const calculateScore = (answers, questions) => {
  let totalScore = 0
  answers.forEach((answer, index) => {
    const question = questions[index]
    const selectedOption = question.options.find(opt => opt.value === answer)
    if (selectedOption) {
      totalScore += selectedOption.score
    }
  })
  return totalScore
}

// 添加结果描述生成函数
const generateResultDescription = (score) => {
  if (score >= 90) {
    return '您的心理状态非常健康，请继续保持！'
  } else if (score >= 75) {
    return '您的心理状态良好，建议继续保持积极的生活态度。'
  } else if (score >= 60) {
    return '您的心理状态一般，建议适当关注自己的心理健康。'
  } else {
    return '您的心理状态需要关注，建议及时寻求专业帮助。'
  }
}

// 添加建议生成函数
const generateSuggestions = (score) => {
  const suggestions = [
    '保持规律的作息时间',
    '适当进行体育运动',
    '培养积极的兴趣爱好'
  ]
  
  if (score < 60) {
    suggestions.unshift(
      '建议您及时咨询心理健康专家',
      '全国心理咨询热线：400-161-9995',
      '建议到专业医疗机构进行心理健康检查'
    )
  }
  
  return suggestions
}
</script>
<style lang="scss" scoped>
.psychological-test {
  @apply space-y-6 p-6;
}

.card-header {
  @apply flex items-center justify-between;
  
  h2 {
    @apply text-xl font-semibold m-0;
  }
  
  .header-left {
    @apply flex items-center gap-4;
    
    .return-btn {
      @apply hover:bg-gray-100 transition-colors;
      
      .back-icon {
        @apply text-xl;
      }
    }
  }
  
  .header-right {
    @apply flex items-center gap-4;
  }
}

.progress-info {
  @apply text-center text-gray-600 mb-6 text-lg;
}

.question-area {
  @apply space-y-8;
  
  .question-content {
    @apply space-y-4;
    
    h3 {
      @apply text-xl font-medium;
    }
    
    .question-desc {
      @apply text-gray-600;
    }
  }
  
  .answer-options {
    @apply flex flex-col gap-4;
    
    .el-radio {
      @apply w-full h-12 flex items-center border rounded-lg px-4;
      margin: 0 !important;
      
      :deep(.el-radio__label) {
        @apply inline-block w-full;
      }
      
      &.is-bordered {
        @apply min-h-[40px] flex items-center px-4;
        
        :deep(.el-radio__input) {
          @apply flex-shrink-0 ml-0;
        }
        
        :deep(.el-radio__label) {
          @apply ml-2;
          white-space: normal;
          line-height: 1.5;
          text-align: left;
        }
      }
    }
  }
  
  .question-nav {
    @apply flex justify-between mt-8;
  }
}

.test-result {
  .result-header {
    @apply flex items-center justify-between mb-6;
    
    h3 {
      @apply text-xl font-bold m-0;
    }
  }
  
  .result-content {
    @apply space-y-6;
    
    .result-desc {
      @apply text-gray-700 leading-relaxed;
    }
    
    .result-suggestions {
      @apply space-y-4;
      
      h4 {
        @apply font-medium;
      }
      
      ul {
        @apply list-disc list-inside space-y-2;
      }
    }
  }
}

// 暗色主题适配
:deep(.dark) {
  .question-content {
    .question-desc {
      @apply text-gray-400;
    }
  }
  
  .result-content {
    .result-desc {
      @apply text-gray-300;
    }
  }
}

.test-type-options {
  @apply p-4;
  
  .radio-group {
    @apply flex flex-col gap-4 w-full;
  }
  
  .radio-item {
    @apply w-full h-12 flex items-center border rounded-lg px-4 mb-0;
    margin: 0 !important; // 覆盖 Element Plus 默认样式
    
    &:deep(.el-radio__label) {
      @apply flex-1;
    }
    
    &:deep(.el-radio__input) {
      @apply flex-shrink-0;
    }
  }
}

// 问卷题目选项样式
.question-options {
  @apply space-y-4 mt-4;
  
  .el-radio {
    @apply w-full h-12 flex items-center border rounded-lg px-4;
    margin: 0 !important;
    
    &:deep(.el-radio__label) {
      @apply flex-1;
    }
    
    &:deep(.el-radio__input) {
      @apply flex-shrink-0;
    }
  }
}

// 确保所有选项都有相同的高度和对齐方式
:deep(.el-radio) {
  height: 48px;
  margin: 8px 0;
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
  
  .el-radio__input {
    margin-right: 10px;
  }
  
  .el-radio__label {
    white-space: normal;
    line-height: 1.5;
  }
}

.dialog-footer {
  @apply flex justify-end gap-4 mt-4;
}
</style> 
