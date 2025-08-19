import './SearchSection.css';
import { PURPLE_PRIMARY } from '../../../utils/bidderConstants';

const SearchSection = ({ searchTerm, handleSearch, category, handleCategoryFilter }) => {
    return (
        <div className="row mb-5">
            <div className="col-md-8">
                <div className="input-group">
                    <input
                        type="text"
                        className="form-control form-control-lg"
                        placeholder="Search auction items..."
                        value={searchTerm}
                        onChange={handleSearch}
                    />
                    <button className="btn btn-custom" type="button" style={{ backgroundColor: PURPLE_PRIMARY }}>
                        <i className="bi bi-search"></i> Search
                    </button>
                </div>
            </div>
            <div className="col-md-4">
                <select
                    className="form-select form-select-lg"
                    value={category}
                    onChange={handleCategoryFilter}
                >
                    <option value="">All Categories</option>
                    <option value="electronics">Electronics</option>
                    <option value="art & collectibles">Art & Collectibles</option>
                    <option value="jewelry">Jewelry</option>
                    <option value="vehicles">Vehicles</option>
                    <option value="real estate">Real Estate</option>
                </select>
            </div>
        </div>
    );
};

export default SearchSection;