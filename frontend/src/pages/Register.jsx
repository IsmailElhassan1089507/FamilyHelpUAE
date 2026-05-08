import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Register = () => {
  const [formData, setFormData] = useState({
    familyName: '',
    email: '',
    password: '',
    region: 'Khalifa City' // Default region
  });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/register', formData);
      await login(response.data.token);
      navigate('/');
    } catch (err) {
      setError('Registration failed. Email might already exist.');
    }
  };

  return (
    <div className="container" style={{ maxWidth: '500px', marginTop: '10vh', marginBottom: '10vh' }}>
      <div className="card">
        <h2 className="text-center mb-4" style={{ color: 'var(--primary-color)' }}>Register Family</h2>
        {error && <div style={{ color: 'var(--danger-color)', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Family Name</label>
            <input 
              type="text" 
              name="familyName"
              className="form-input" 
              value={formData.familyName}
              onChange={handleChange}
              required 
            />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input 
              type="email" 
              name="email"
              className="form-input" 
              value={formData.email}
              onChange={handleChange}
              required 
            />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input 
              type="password" 
              name="password"
              className="form-input" 
              value={formData.password}
              onChange={handleChange}
              required 
              minLength="6"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Region</label>
            <select 
              name="region" 
              className="form-input" 
              value={formData.region} 
              onChange={handleChange}
            >
              <option value="Khalifa City">Khalifa City</option>
              <option value="Al Reem Island">Al Reem Island</option>
              <option value="Mussafah">Mussafah</option>
              <option value="Baniyas">Baniyas</option>
              <option value="Mohammed Bin Zayed City">Mohammed Bin Zayed City</option>
              <option value="Al Nahyan">Al Nahyan</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary mt-4" style={{ width: '100%' }}>Register</button>
        </form>
        <p className="text-center mt-4">
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
