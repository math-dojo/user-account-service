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
    },
    badOrgModificationRequest: {
        invalidParam: "ivalidValue"
    },
    newUserRequest: {
    	id: "newUser",
        accountVerified: false,
        name: "some name",
        profileImageLink: "https://my.image.domain.com/it.jpg"     
    },
    userWithKnownId: {
    	id: "knownUserId",
        accountVerified: false,
        name: "some name",
        profileImageLink: "https://my.image.domain.com/it.jpg"     
    },
    badNewUserRequest: {
        accountVerified: true,
        name: "",
        profileImageLink: "https://my.image.domain.com/it.jpg"
    },
    userModificationRequest: {
        accountVerified: true,
        name: "some Newname",
        profileImageLink: "https://my.image.domain.com/new-image.jpg"
    },
    badUserModificationRequest: {
        invalidParam: "ivalidValue"
    },
    userPermissionsModificationRequest: {
        permissions: ["CONSUMER", "CREATOR"]
    },
    badUserPermissionsModificationRequest: {
        invalidParam: "ivalidValue",
        permissions: ["spammyPermission"]
    }
};
