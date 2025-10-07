import React from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import RegisterPage from './pages/Register';
import LoginPage from './pages/Login';
import Dashboard from './pages/Dashboard';
import ProfilesPage from './pages/Profiles';
import DocumentsPage from './pages/Documents';

function App() {
  return (
    <Router>
      <Routes>
        <Route path='/register' element={<RegisterPage/>}/>
        <Route path='/login' element={<LoginPage/>}/>
        <Route path='/' element={<Dashboard />}/>
        <Route path="/profiles" element={<ProfilesPage />}/>
        <Route path="/documents" element={<DocumentsPage/>}/>
      </Routes>
    </Router>
  );
}

export default App;
