import React, { useContext } from 'react';
import { AuthContext } from '../App';

const ProfilePage: React.FC = () => {
  const { user } = useContext(AuthContext);

  if (!user) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-600">Please log in to view your profile.</div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">My Profile</h1>
      
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex items-center space-x-6 mb-6">
          <div className="w-20 h-20 bg-gray-200 rounded-full flex items-center justify-center text-2xl">
            👤
          </div>
          <div>
            <h2 className="text-xl font-semibold text-gray-900">{user.username}</h2>
            <p className="text-gray-600">{user.email}</p>
          </div>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Username
            </label>
            <div className="text-gray-900">{user.username}</div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <div className="text-gray-900">{user.email}</div>
          </div>
          
          {user.firstName && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                First Name
              </label>
              <div className="text-gray-900">{user.firstName}</div>
            </div>
          )}
          
          {user.lastName && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Last Name
              </label>
              <div className="text-gray-900">{user.lastName}</div>
            </div>
          )}
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Roles
            </label>
            <div className="flex space-x-2">
              {user.roles.map((role) => (
                <span
                  key={role}
                  className="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded"
                >
                  {role}
                </span>
              ))}
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Account Status
            </label>
            <div className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${
              user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}>
              {user.enabled ? '✅ Active' : '❌ Inactive'}
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Member Since
            </label>
            <div className="text-gray-900">
              {new Date(user.createdAt).toLocaleDateString()}
            </div>
          </div>
        </div>
        
        <div className="mt-8 pt-6 border-t border-gray-200">
          <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-4">
            Edit Profile
          </button>
          <button className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600">
            Change Password
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;