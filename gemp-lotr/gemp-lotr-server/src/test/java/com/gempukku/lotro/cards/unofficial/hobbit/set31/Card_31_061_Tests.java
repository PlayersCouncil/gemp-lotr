package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_061_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer, and
	// William's site is 2, so he is not roaming there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("william", "31_61");

					// Minions to discard for the play, and not
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a maneuver ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc; an end-of-Shadow-phase offer with nothing to take
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; texts on assigning him, a regroup ability
					// Cards in hand whose cost is read
					put("bert", "31_56");       // Bert, Troll of Ettenmoors: [troll] Troll, cost 5; his texts work only in play
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll, cost 5; a skirmish ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an assignment ability not used

					// Followers, and what transfers them
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower, "Aid - Exert Bilbo"; a bar on overwhelming bearer
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower; a trigger on a fierce skirmish won
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: [gandalf] follower, "Skirmish: Transfer Gwaihir to your support area to cancel a skirmish involving bearer and a mounted Orc"
					put("saruman", "17_38");    // Saruman, Servant of Sauron: [isengard] follower of the Shadow side, "Aid - (1)" to a minion; a trigger on bearer winning
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; strength +1 for each [dwarven] follower, a fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used

					// Hand weapons of both sides and three cultures, a possession and an artifact
					put("axe", "31_1");         // Dwarven Axe: [dwarven] hand weapon possession; damage +1 at a mountain only
					put("warhammer", "33_7");   // Dwarven Warhammer: [dwarven] hand weapon, "Maneuver: Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer"
					put("sting", "30_47");      // Sting: [shire] hand weapon artifact on Bilbo; a trigger on Bilbo winning
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon on an Orc; a text on its own play only
					// A weapon of another class, and a possession that is no weapon
					put("bow", "33_40");        // Goblin Bow: [moria] ranged weapon on a [moria] Orc; archer, an archery ability not used
					put("warg", "32_32");       // Vicious Warg: [moria] mount on an Orc; bearer is fierce, a text on its own play only
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc to bear them; a text on his own play only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WilliamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: William, Troll of Ettenmoors
		 * Unique: True
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>. To play, discard an Orc.
		 * The twilight cost of each Troll is -2.
		 * Each time the Free Peoples player transfers a follower, you may discard a hand weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("william");

		assertEquals("William", card.getBlueprint().getTitle());
		assertEquals("Troll of Ettenmoors", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingWilliamDiscardsAnOrcAndCostsHisFullFiveThenEachTrollInHandCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var yazneg = scn.GetShadowCard("yazneg");
		var malevolent = scn.GetShadowCard("malevolent");
		var ferny = scn.GetShadowCard("ferny");
		var bert = scn.GetShadowCard("bert");
		var demolition = scn.GetShadowCard("demolition");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(william, bert, demolition, aggressor);

		scn.StartGame();
		assertInZone(Zone.HAND, william);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(yazneg, malevolent, ferny);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilightCost(bert));
		assertEquals(5, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(william));
		scn.ShadowPlayCard(william);
		assertTrue(scn.ShadowHasCardChoiceAvailable(yazneg, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, malevolent);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, william);
		// "Each Troll" does not name William, so it does not lower his own cost as he is played: 5, not 3.
		assertEquals(pool - 5, scn.GetTwilight());
		// With William in play: 5 - 2 for each Troll, of either culture; the Orc is not lowered.
		assertEquals(3, scn.GetTwilightCost(bert));
		assertEquals(3, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WilliamCannotBePlayedUntilAnOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToHand(william, yazneg);

		scn.StartGame();
		assertInZone(Zone.HAND, william);
		assertInZone(Zone.HAND, yazneg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(ferny);
		scn.FreepsPass();

		// Bill Ferny Sr. is no Orc, and the Orc in hand is not in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(william));
		scn.ShadowPlayCard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertTrue(scn.ShadowPlayAvailable(william));
		scn.ShadowPlayCard(william);
		// The one Orc is discarded with nothing asked.
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, william);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAFollowersAidIsPaidWilliamMayDiscardAHandWeaponOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var warhammer = scn.GetFreepsCard("warhammer");
		var sting = scn.GetFreepsCard("sting");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		var bow = scn.GetShadowCard("bow");
		var warg = scn.GetShadowCard("warg");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(balin, warhammer);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(william, sneak);
		scn.AttachCardsTo(sneak, scimitar, bow, warg);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(warhammer, balin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(scimitar, sneak);
		assertAttachedTo(bow, sneak);
		assertAttachedTo(warg, sneak);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(william));
		scn.ShadowAcceptOptionalTrigger();
		// Hand weapons of both sides, possessions and an artifact; not the ranged weapon, nor the mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, warhammer, sting, scimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bow, warg));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);
		assertInDiscard(axe);
		assertAttachedTo(warhammer, balin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(scimitar, sneak);
		assertAttachedTo(bow, sneak);
		assertAttachedTo(warg, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(william, sneak);
		scn.AttachCardsTo(sneak, scimitar);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(scimitar, sneak);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(william));
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(axe, thorin);
		assertAttachedTo(scimitar, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionTransferredOrAFollowerPlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(dori);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(bilbo, sting);
		// William is cheated in during the fellowship phase, only to read that nothing here sets him off.
		scn.MoveMinionsToTable(william);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertInHand(dori);
		scn.FreepsRefreshDecision();

		// A possession transferred by the Free Peoples player is no follower.
		assertTrue(scn.FreepsTransferAvailable(axe));
		// Balin is the one other [dwarven] character to bear it: nothing is asked.
		scn.FreepsTransferCard(axe);
		assertAttachedTo(axe, balin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(william));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A follower played goes to the support area, and is not transferred.
		scn.FreepsPlayCard(dori);
		assertInZone(Zone.SUPPORT, dori);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(william));
		assertAttachedTo(axe, balin);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHandWeaponInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var sneak = scn.GetShadowCard("sneak");
		var bow = scn.GetShadowCard("bow");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(william, sneak);
		scn.AttachCardsTo(sneak, bow, warg);
		assertAttachedTo(bow, sneak);
		assertAttachedTo(warg, sneak);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(william));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachTransferOffersAgainTheWarhammersAttachIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var dori = scn.GetFreepsCard("dori");
		var bifur = scn.GetFreepsCard("bifur");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var warhammer = scn.GetFreepsCard("warhammer");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		// Two cards for the Warhammer's discard from hand
		var aggressor = scn.GetFreepsCard("aggressor");
		var malevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.MoveCardsToHand(aggressor, malevolent);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(balin, warhammer);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(william, sneak);
		scn.AttachCardsTo(sneak, scimitar);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(warhammer, balin);
		assertAttachedTo(scimitar, sneak);
		assertInZone(Zone.SUPPORT, dori);
		assertInHand(aggressor, malevolent);
		scn.SkipToPhase(Phase.MANEUVER);

				scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(william));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Bifur is laid only now, so that his own Aid is not offered beside Dori's.
		scn.MoveCardsToSupportArea(bifur);
		assertInZone(Zone.SUPPORT, bifur);
		scn.FreepsRefreshDecision();
		// Dwarven Warhammer "attaches" a follower: a transfer in the Hobbit sets (Q-0407: B).
		scn.FreepsUseCardAction(warhammer);
		assertInDiscard(aggressor, malevolent);
		assertAttachedTo(bifur, balin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(william));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(scimitar);
		assertInDiscard(scimitar);
		assertAttachedTo(warhammer, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerTransferringHisOwnFollowerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var saruman = scn.GetShadowCard("saruman");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToSupportArea(saruman);
		scn.MoveMinionsToTable(william, sneak);
		scn.AttachCardsTo(sneak, scimitar);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(scimitar, sneak);
		assertInZone(Zone.SUPPORT, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Saruman's Aid, (1), is the Shadow player's, to a minion.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sneak);
		assertAttachedTo(saruman, sneak);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(william));
		assertAttachedTo(axe, thorin);
		assertAttachedTo(scimitar, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void GwaihirTransferredBackToTheSupportAreaOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		// Q-0410 (Ketura, B): only a follower transferred onto a character counts; a follower's own move back to the
		// support area does not ("we dont' need to trigger off of follower returns").
		var scn = GetScenario();

		var william = scn.GetShadowCard("william");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var sneak = scn.GetShadowCard("sneak");
		var warg = scn.GetShadowCard("warg");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe, gwaihir);
		scn.MoveMinionsToTable(william, sneak);
		// The Warg makes the Sneak a mounted Orc.
		scn.AttachCardsTo(sneak, warg);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(gwaihir, thorin);
		assertAttachedTo(warg, sneak);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, sneak}, new PhysicalCardImpl[]{bilbo, william});
		scn.FreepsResolveSkirmish(thorin);

		assertTrue(scn.FreepsActionAvailable(gwaihir));
		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(william));
		assertAttachedTo(axe, thorin);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
	}
}
