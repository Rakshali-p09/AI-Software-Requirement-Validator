
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import CreateProject from "./pages/CreateProject";
import ProjectDetails from "./pages/ProjectDetails";
import UploadSRS from "./pages/UploadSRS";
import ValidationResults from "./pages/ValidationResults";
import ProtectedRoute from "./components/ProtectedRoute";

import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Home */}
        <Route path="/" element={<Home />} />

        {/* Authentication */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected Dashboard */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Protected Project Pages */}
        <Route
          path="/create-project"
          element={
            <ProtectedRoute>
              <CreateProject />
            </ProtectedRoute>
          }
        />

        <Route
          path="/project/:id"
          element={
            <ProtectedRoute>
              <ProjectDetails />
            </ProtectedRoute>
          }
        />

        {/* Protected SRS Upload */}
        <Route
          path="/upload"
          element={
            <ProtectedRoute>
              <UploadSRS />
            </ProtectedRoute>
          }
        />

        {/* Protected Validation Results */}
        <Route
          path="/validation-results"
          element={
            <ProtectedRoute>
              <ValidationResults />
            </ProtectedRoute>
          }
        />

        {/* Invalid URL */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
