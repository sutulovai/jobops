import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { RequireAuth } from './app/router'
import { AppLayout } from './components/layout/AppLayout'
import { LoginPage } from './features/auth/LoginPage'
import { RegisterPage } from './features/auth/RegisterPage'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { AnalyzePage } from './features/analyze/AnalyzePage'
import { PipelinePage } from './features/pipeline/PipelinePage'
import { CompaniesPage } from './features/companies/CompaniesPage'
import { CompanyDetailPage } from './features/companies/CompanyDetailPage'
import { VacanciesPage } from './features/vacancies/VacanciesPage'
import { VacancyDetailPage } from './features/vacancies/VacancyDetailPage'
import { ContactsPage } from './features/contacts/ContactsPage'
import { MessagesPage } from './features/messages/MessagesPage'
import { SearchesPage } from './features/searches/SearchesPage'
import { AnalyticsPage } from './features/analytics/AnalyticsPage'
import { WeeklyReviewPage } from './features/weekly/WeeklyReviewPage'
import { ProfilePage } from './features/profile/ProfilePage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <AppLayout />
            </RequireAuth>
          }
        >
          <Route index element={<Navigate to="/today" replace />} />
          <Route path="today" element={<DashboardPage />} />
          <Route path="analyze" element={<AnalyzePage />} />
          <Route path="pipeline" element={<PipelinePage />} />
          <Route path="companies" element={<CompaniesPage />} />
          <Route path="companies/:id" element={<CompanyDetailPage />} />
          <Route path="vacancies" element={<VacanciesPage />} />
          <Route path="vacancies/:id" element={<VacancyDetailPage />} />
          <Route path="contacts" element={<ContactsPage />} />
          <Route path="messages" element={<MessagesPage />} />
          <Route path="searches" element={<SearchesPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="review" element={<WeeklyReviewPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
