package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_060_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; turn 1's maneuver
	// phase and skirmishes are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "32_60");

					// Followers, and what transfers them
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower, "Aid - Exert Bilbo"; a bar on overwhelming bearer
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower; a trigger on a fierce skirmish won
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: [gandalf] follower, "Skirmish: Transfer Gwaihir to your support area to cancel a skirmish involving bearer and a mounted Orc"
					put("saruman", "17_38");    // Saruman, Servant of Sauron: [isengard] follower of the Shadow side, "Aid - (1)" to a minion; a trigger on bearer winning
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; strength +1 for each [dwarven] follower, a fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, archer; a regroup ability not used

					// Weapons of both sides: hand and ranged, possessions and an artifact
					put("axe", "31_1");         // Dwarven Axe: [dwarven] hand weapon possession; damage +1 at a mountain only
					put("warhammer", "33_7");   // Dwarven Warhammer: [dwarven] hand weapon, "Maneuver: Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer"
					put("sting", "30_47");      // Sting: [shire] hand weapon artifact on Bilbo; a trigger on Bilbo winning
					put("arrow", "32_16");      // Black Arrow: [esgaroth] ranged weapon possession on Bard; an archery ability not used
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon on an Orc; a text on its own play only
					put("bow", "33_40");        // Goblin Bow: [moria] ranged weapon on a [moria] Orc; archer, an archery ability not used
					// A possession that is no weapon
					put("warg", "32_32");       // Vicious Warg: [moria] mount on an Orc; bearer is fierce, a text on its own play only
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc to bear them; a text on his own play only
					// Two cards for the Warhammer's discard from hand
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Cantëa, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * Each time the Free Peoples player transfers a follower, you may exert Úlairë Cantëa to discard a weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenAFollowersAidIsPaidCanteaMayExertToDiscardAWeaponOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var bard = scn.GetFreepsCard("bard");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var arrow = scn.GetFreepsCard("arrow");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		var bow = scn.GetShadowCard("bow");
		var warg = scn.GetShadowCard("warg");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(bard);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(bilbo, sting);
		scn.AttachCardsTo(bard, arrow);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(cantea, sneak);
		scn.AttachCardsTo(sneak, scimitar, bow, warg);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(arrow, bard);
		assertAttachedTo(scimitar, sneak);
		assertAttachedTo(bow, sneak);
		assertAttachedTo(warg, sneak);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cantea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(cantea));
		// Hand and ranged weapons of both sides, possessions and an artifact; not the mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, sting, arrow, scimitar, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(arrow);
		assertInDiscard(arrow);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(scimitar, sneak);
		assertAttachedTo(bow, sneak);
		assertAttachedTo(warg, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningExertsNothingAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(cantea);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cantea));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, thorin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedCanteaIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(cantea);
		// Vitality 3: two wounds leave her nothing to exert.
		scn.AddWoundsToChar(cantea, 2);
		assertEquals(2, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cantea));
		assertEquals(2, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, thorin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoWeaponInPlayCanteaExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var dori = scn.GetFreepsCard("dori");
		var thorin = scn.GetFreepsCard("thorin");
		var sneak = scn.GetShadowCard("sneak");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(cantea, sneak);
		scn.AttachCardsTo(sneak, warg);
		assertAttachedTo(warg, sneak);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(dori, thorin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cantea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(cantea));
		assertAttachedTo(warg, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionTransferredOrAFollowerPlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
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
		// Cantëa is cheated in during the fellowship phase, only to read that nothing here sets her off.
		scn.MoveMinionsToTable(cantea);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(sting, bilbo);
		assertInHand(dori);
		scn.FreepsRefreshDecision();

		// A possession transferred by the Free Peoples player is no follower.
		assertTrue(scn.FreepsTransferAvailable(axe));
		// Balin is the one other [dwarven] character to bear it: nothing is asked.
		scn.FreepsTransferCard(axe);
		assertAttachedTo(axe, balin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cantea));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A follower played goes to the support area, and is not transferred.
		scn.FreepsPlayCard(dori);
		assertInZone(Zone.SUPPORT, dori);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cantea));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, balin);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GwaihirTransferredBackToTheSupportAreaOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var sneak = scn.GetShadowCard("sneak");
		var warg = scn.GetShadowCard("warg");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, axe, gwaihir);
		scn.MoveMinionsToTable(cantea, sneak);
		// The Warg makes the Sneak a mounted Orc.
		scn.AttachCardsTo(sneak, warg);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(gwaihir, thorin);
		assertAttachedTo(warg, sneak);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, sneak}, new PhysicalCardImpl[]{bilbo, cantea});
		scn.FreepsResolveSkirmish(thorin);

		assertTrue(scn.FreepsActionAvailable(gwaihir));
		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		// Only a transfer onto a character counts (Q-0410: B).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cantea));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, thorin);
		assertAttachedTo(warg, sneak);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
	}

	@Test
	public void EachTransferOffersAgainTheWarhammersAttachIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var dori = scn.GetFreepsCard("dori");
		var bifur = scn.GetFreepsCard("bifur");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var warhammer = scn.GetFreepsCard("warhammer");
		var sneak = scn.GetShadowCard("sneak");
		var scimitar = scn.GetShadowCard("scimitar");
		var aggressor = scn.GetFreepsCard("aggressor");
		var malevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.MoveCardsToHand(aggressor, malevolent);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(balin, warhammer);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(cantea, sneak);
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
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cantea));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);
		assertEquals(1, scn.GetWoundsOn(cantea));
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
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cantea));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(scimitar);
		assertEquals(2, scn.GetWoundsOn(cantea));
		assertInDiscard(scimitar);
		assertAttachedTo(warhammer, balin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerTransferringHisOwnFollowerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
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
		scn.MoveMinionsToTable(cantea, sneak);
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
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(cantea));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(axe, thorin);
		assertAttachedTo(scimitar, sneak);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
