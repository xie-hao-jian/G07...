import { defineStore } from 'pinia'

export const useTestRecordsStore = defineStore('testRecords', {
  state: () => ({
    tests: [],
    records: []
  }),
  
  actions: {
    addTest(test) {
      this.tests.unshift(test)
    },
    
    deleteTest(testId) {
      this.tests = this.tests.filter(test => test.id !== testId)
      this.records = this.records.filter(record => record.id !== testId)
    },
    
    updateTest(testId, updates) {
      const index = this.tests.findIndex(test => test.id === testId)
      if (index !== -1) {
        this.tests[index] = { ...this.tests[index], ...updates }
      }
    },
    
    addRecord(record) {
      const existingIndex = this.records.findIndex(r => r.id === record.id)
      if (existingIndex !== -1) {
        this.records[existingIndex] = record
      } else {
        this.records.unshift(record)
      }
    }
  },
  
  persist: true
}) 