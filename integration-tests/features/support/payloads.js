module.exports.payloads = {
    newOrgRequest: {
        accountVerified: false,
        name: "some name",
        profileImageLink: "https://my.image.domain.com/it.jpg"
    },
    badNewOrgRequest: {
        accountVerified: false,
        profileImageLink: "https://my.image.domain.com/it.jpg"
    },
    orgModificationRequest: {
        accountVerified: true,
        name: "some Newname",
        profileImageLink: "https://my.image.domain.com/new-image.jpg"
    }
};
