import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const TaskFeed = () => {
  const { user } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [recommendations, setRecommendations] = useState({});
  const [filter, setFilter] = useState('ALL'); // ALL, REQUEST, OFFER

  useEffect(() => {
    fetchTasks();
  }, [filter]);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      let url = '/tasks';
      if (filter !== 'ALL') {
        url += `?type=${filter}`;
      }
      const response = await api.get(url);
      setTasks(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const acceptTask = async (taskId) => {
    try {
      await api.post(`/tasks/${taskId}/accept`);
      fetchTasks();
    } catch (err) {
      alert('Failed to accept task. It might no longer be open or you are the creator.');
    }
  };

  const loadRecommendations = async (taskId) => {
    try {
      const response = await api.get(`/recommendations/tasks/${taskId}/suitable-families`);
      setRecommendations(prev => ({...prev, [taskId]: response.data}));
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="container mt-8">
      <div className="flex justify-between items-center mb-8">
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>Community Task Feed</h1>
        <div className="flex gap-4">
          <select 
            className="form-input" 
            style={{ width: 'auto' }}
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="ALL">All Tasks</option>
            <option value="REQUEST">Requests</option>
            <option value="OFFER">Offers</option>
          </select>
          <button className="btn btn-primary">Create Task</button>
        </div>
      </div>

      {loading ? (
        <div>Loading tasks...</div>
      ) : (
        <div className="grid-layout">
          {tasks.map(task => (
            <div key={task.taskId} className="card">
              <div className="flex justify-between items-center mb-2">
                <span style={{ 
                  backgroundColor: task.type === 'REQUEST' ? '#FEF3C7' : '#D1FAE5',
                  color: task.type === 'REQUEST' ? '#92400E' : '#065F46',
                  padding: '0.25rem 0.75rem',
                  borderRadius: '9999px',
                  fontSize: '0.75rem',
                  fontWeight: 'bold'
                }}>
                  {task.type}
                </span>
                <span className="text-muted" style={{ fontSize: '0.875rem' }}>{task.status}</span>
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>{task.title}</h3>
              <p className="text-muted mb-4">{task.description}</p>
              
              <div style={{ fontSize: '0.875rem', marginBottom: '1rem' }}>
                <div><strong>Category:</strong> {task.category || 'General'}</div>
                <div><strong>Region:</strong> {task.region || 'Any'}</div>
                <div><strong>Posted By:</strong> {task.postedByFamilyId === user?.familyId ? 'You' : 'Community Member'}</div>
              </div>

              {task.status === 'OPEN' && task.postedByFamilyId !== user?.familyId && (
                <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => acceptTask(task.taskId)}>
                  Accept Task
                </button>
              )}

              {task.status === 'OPEN' && task.postedByFamilyId === user?.familyId && (
                <div className="mt-4">
                  <button className="btn btn-secondary" style={{ width: '100%', fontSize: '0.875rem' }} onClick={() => loadRecommendations(task.taskId)}>
                    Find Suitable Families
                  </button>
                  {recommendations[task.taskId] && (
                    <div className="mt-4" style={{ backgroundColor: 'var(--bg-color)', padding: '1rem', borderRadius: 'var(--radius)' }}>
                      <h4 style={{ marginBottom: '0.5rem', fontSize: '0.875rem' }}>Top Recommendations</h4>
                      {recommendations[task.taskId].map(rec => (
                        <div key={rec.familyId} style={{ marginBottom: '0.5rem', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border-color)', fontSize: '0.875rem' }}>
                          <div><strong>{rec.familyName}</strong> (Score: {rec.recommendationScore.toFixed(1)})</div>
                          <div className="text-muted" style={{ fontSize: '0.75rem' }}>{rec.reason}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
          {tasks.length === 0 && (
            <div className="text-center text-muted" style={{ gridColumn: '1 / -1', padding: '3rem' }}>
              No tasks found. Be the first to create one!
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default TaskFeed;
