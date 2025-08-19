import './Packages.css';
import { PURPLE_PRIMARY } from '../../../utils/bidderConstants';

const Packages = ({ packages, setPackageData }) => {
    return (
        <section id="packages" className="mb-5">
            <h2 className="section-title">
                <i className="bi bi-box me-2"></i>Auction Packages
            </h2>
            <div className="row">
                {packages.map((pkg) => (
                    <div className="col-md-4 mb-4" key={pkg.name}>
                        <div className="card package-card">
                            <div className="card-body text-center">
                                <i className={`bi ${pkg.icon} fa-3x ${pkg.iconColor} mb-3`}></i>
                                <h5 className="card-title">{pkg.name} Package</h5>
                                <h3 style={{ color: PURPLE_PRIMARY }}>${pkg.price.toFixed(2)}</h3>
                                <ul className="list-unstyled">
                                    {pkg.features.map((feature, index) => (
                                        <li key={index}>
                                            <i className="bi bi-check-circle text-success me-2"></i>
                                            {feature}
                                        </li>
                                    ))}
                                </ul>
                                <button
                                    className="btn btn-custom w-100"
                                    data-bs-toggle="modal"
                                    data-bs-target="#buyPackageModal"
                                    onClick={() => setPackageData(pkg)}
                                >
                                    Buy Package
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
};

export default Packages;