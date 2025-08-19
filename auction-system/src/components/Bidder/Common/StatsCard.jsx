import { PURPLE_PRIMARY } from '../../../utils/bidderConstants';

const StatsCard = ({ title, value, icon }) => {
    return (
        <div className="card mb-4 shadow-sm">
            <div className="card-body d-flex align-items-center">
                <i className={`bi ${icon} fa-2x me-3`} style={{ color: PURPLE_PRIMARY }}></i>
                <div>
                    <h5 className="card-title mb-0">{title}</h5>
                    <p className="card-text" style={{ color: PURPLE_PRIMARY }}>{value}</p>
                </div>
            </div>
        </div>
    );
};

export default StatsCard;