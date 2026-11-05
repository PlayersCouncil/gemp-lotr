package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comedown", "4_146");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde: Elf ally, home site 3, vitality 2; has the text of site 3 while it can be spotted
					put("celeborn", "1_34");    // Celeborn: Elf ally, home site 6, vitality 3; a fellowship ability used only on purpose
					put("orophin", "1_56");     // Orophin: Elf ally and archer, home site 6; an archery ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, a Man, vitality 1, home site 3T; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, vitality 3, damage +1 and nothing else; not an archer
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai archer, damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer; its when-played token does nothing when cheated in
					put("bowmen", "4_138");     // Band of Uruk Bowmen: [isengard] Uruk-hai archer; +1 archery total only while its player controls a site (none here)
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, nothing else
					// Lurtz, Halfling Hunter: unique [uruk-hai] Uruk-hai archer (the race and keyword without the culture); a
					// maneuver and an assignment ability used only on purpose
					put("lurtz", "18_118");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStreetsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comedown", "4_146");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
					put("aldor", "4_262");      // Aldor: [rohan] Man ally, home sites 3T and 6T, vitality 2; an archery ability that needs a villager (none here)
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai archer, damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer; its when-played token does nothing when cheated in
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: a fellowship ability used only on purpose
					put("site2", "4_331");      // Eastfold: a Shadow ability that needs 3 [isengard] trackers
					put("site3", "4_340");      // Streets of Edoras: each [rohan] ally may not participate in archery fire or skirmishes
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComeDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Come Down
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Archery:</b> Spot 2 [isengard] archers to make allies take wounds from archery fire instead of companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("comedown");

		assertEquals("Come Down", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void ArcheryWithTwoIsengardArchersSendsArcheryWoundsToAlliesAtHomeOrAwayAndNotCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, celeborn, merchant);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin, crossbow);
		// The fellowship moves to site 3, Silinde's home; Celeborn's is site 6.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Minion archery total 2, both placed by the Free Peoples player on allies. The Gondorian Merchant (a [gondor]
		// Man, home on the Towers sites) is offered beside the two Elves; he is not chosen, since one wound would kill him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(celeborn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(silinde);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryIsNotOfferedWithOneIsengardArcherBesideArchersOfOtherCulturesAndAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, comedown);
		scn.MoveMinionsToTable(assassin, marksman, savage, lurtz);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// One [isengard] archer; the [moria] Orc archer and the [uruk-hai] Uruk-hai archer have the keyword without the
		// culture, the Savage the culture without the keyword.  An ally is in play to take the wounds.

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
	}

	@Test
	public void ArcheryIsNotOfferedWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Two [isengard] archers, and no ally to take a wound: the ability has no cost and would do nothing.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
	}

	@Test
	public void ArcheryIsNotOfferedWhenTheOnlyAllyIsBarredFromArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// At site 2 nothing bars Aldor, the one ally: he could be made to take the wounds, and the ability is offered.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowPass();
		// Not used: the wounds go as usual, to companions (Aldor is away from home).
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aldor));

		// No skirmishes; the fellowship moves on to site 3, Streets of Edoras, and there is an archery phase again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();

		// The site bars Aldor from archery fire, so no wound can be sent to him: with no other ally in play the
		// ability has no cost and would do nothing (Q-0147, Q-0034).
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
		scn.ShadowPass();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor));
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheAlliesTakeArcheryWoundsOnlyInTheArcheryPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		scn.PassArcheryActions();
		// Celeborn, the one ally, is the only character the 2 wounds can go to.
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// No skirmishes; the fellowship moves on to site 3, and the archers fire again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryWoundsTheAlliesCannotTakeGoToCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var bowmen = scn.GetShadowCard("bowmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow, bowmen);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Minion archery total 3; Silinde, the one ally, takes two and is killed (vitality 2).
		assertInZone(Zone.DEAD, silinde);

		// The third wound cannot go to an ally, so it is placed as it would have been without Come Down.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnArcherAllyAwayFromHomeDoesNotAddToTheFellowshipArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Orophin's home is site 6; the fellowship is at site 2.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.ShadowUseCardAction(comedown);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
