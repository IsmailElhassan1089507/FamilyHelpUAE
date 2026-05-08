import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import api from '../services/api';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const response = await api.get('/tasks');
        setTasks(response.data);
      } catch (err) {
        console.error("Failed to fetch tasks", err);
      } finally {
        setLoading(false);
      }
    };
    fetchTasks();
  }, []);

  return (
    <div className="container">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>Welcome back, {user?.familyName}!</h1>
          <p className="text-muted">Trust Score: {user?.trustScore} • Region: {user?.region}</p>
        </div>
        <button className="btn btn-secondary" onClick={logout}>Logout</button>
      </div>

      <div className="grid-layout">
        <div className="card stat-card">
          <h3>Your Active Tasks</h3>
          <p className="stat-number">{tasks.filter(t => t.postedByFamilyId === user?.familyId && t.status === 'OPEN').length}</p>
        </div>
        <div className="card stat-card">
          <h3>Tasks Accepted</h3>
          <p className="stat-number">{tasks.filter(t => t.acceptedByFamilyId === user?.familyId && t.status === 'IN_PROGRESS').length}</p>
        </div>
        <div className="card stat-card">
          <h3>Completed Connections</h3>
          <p className="stat-number">{tasks.filter(t => t.status === 'COMPLETED').length}</p>
        </div>
      </div>

      <div className="flex gap-4 mt-8">
        <Link to="/tasks" className="btn btn-primary">Browse Task Feed</Link>
        <Link to="/network" className="btn btn-secondary">View Community Network</Link>
      </div>
    </div>
  );
};

export default Dashboard;
