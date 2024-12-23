import { defineStore } from 'pinia'
import type { Book } from '@/types'
import { processImageUrl } from '@/api/book'

interface State {
  collections: Book[]
}

export const useCollectionsStore = defineStore('collections', {
  state: (): State => ({
    collections: []
  }),

  getters: {
    isCollected: (state) => (bookId: number) => {
      return state.collections.some(book => book.id === bookId)
    }
  },

  actions: {
    addCollection(book: Book) {
      if (!this.isCollected(book.id)) {
        const bookToSave = { ...book }
        if (bookToSave.coverUrl?.startsWith('http')) {
          bookToSave.coverUrl = bookToSave.coverUrl.replace(new URL(bookToSave.coverUrl).origin, '')
        }
        this.collections.push(bookToSave)
        this.saveToLocalStorage()
      }
    },

    removeCollection(bookId: number) {
      const index = this.collections.findIndex(book => book.id === bookId)
      if (index !== -1) {
        this.collections.splice(index, 1)
        this.saveToLocalStorage()
      }
    },

    toggleCollection(book: Book) {
      if (this.isCollected(book.id)) {
        this.removeCollection(book.id)
        return false
      } else {
        this.addCollection(book)
        return true
      }
    },

    saveToLocalStorage() {
      try {
        const validCollections = this.collections.filter(book => book && book.id && book.name)
        localStorage.setItem('collections', JSON.stringify(validCollections))
      } catch (error) {
        console.error('Failed to save collections:', error)
      }
    },

    loadFromLocalStorage() {
      try {
        const saved = localStorage.getItem('collections')
        if (!saved) {
          this.collections = []
          return
        }
        
        const parsedCollections = JSON.parse(saved)
        if (!Array.isArray(parsedCollections)) {
          console.warn('Invalid collections data format')
          this.collections = []
          return
        }
        
        this.collections = parsedCollections
          .filter(book => book && book.id && book.name)
          .map((book: Book) => ({
            ...book,
            coverUrl: book.coverUrl ? processImageUrl(book.coverUrl) : ''
          }))
      } catch (error) {
        console.error('Failed to parse collections:', error)
        this.collections = []
      }
    },

    initialize() {
      this.loadFromLocalStorage()
    }
  }
}) 