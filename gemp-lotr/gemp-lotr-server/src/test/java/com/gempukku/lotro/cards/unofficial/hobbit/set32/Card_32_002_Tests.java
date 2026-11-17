package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_002_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow and
	// regroup phases of turn 1 are at The Troll Hoard (site 2); a regroup move takes the fellowship on to Rivendell
	// (site 3). Smaug is discarded anywhere but at a mountain, so his board is at Erebor (site 7). The minions are
	// laid in the Shadow phase and live through an assignment phase in which nobody is assigned.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dain", "32_2");

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7 (+1 for each [dwarven] follower he bears)
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion who is no Thorin, strength 7; a maneuver ability not used
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower; his Aid declined
					put("ori", "30_18");        // Ori, Dwarven Chronicler: [dwarven] follower; his transfer trigger names Balin, not in play
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: a [shire] follower; his Aid declined
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession that is no follower, on Thorin

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc; a maneuver ability at a battleground
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man; his texts answer [esgaroth] allies, none in play
					put("smaug", "30_66");      // Smaug, The Golden: [smaug] Dragon, discarded if not at a mountain; a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DainIronfootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dain Ironfoot, Lord of the Iron Hills
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Dwarf
		 * Home: 9H
		 * Strength: 7
		 * Vitality: 3
		 * Game Text: Thorin is strength +1.
		 * 	At the start of the regroup phase, you may discard a [dwarven] follower to discard a minion (except Smaug).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dain");

		assertEquals("Dain Ironfoot", card.getBlueprint().getTitle());
		assertEquals("Lord of the Iron Hills", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 9)));
	}

	@Test
	public void ThorinIsStrengthPlusOneAndAnotherDwarvenCompanionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var thorin = scn.GetFreepsCard("thorin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(thorin, nori);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(nori));

		scn.MoveAlliesToTable(dain);

		assertEquals(8, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(nori));
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseDiscardsADwarvenFollowerToDiscardAMinionOtherThanSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var thorin = scn.GetFreepsCard("thorin");
		var dori = scn.GetFreepsCard("dori");
		var ori = scn.GetFreepsCard("ori");
		var thrush = scn.GetFreepsCard("thrush");
		var axe = scn.GetFreepsCard("axe");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		// On to Esgaroth, and from there a second move, in the regroup phase, to Erebor.
		// Dáin, a [dwarven] ally, laid in the draw deck: Esgaroth's start-of-regroup offer is made only with such an
		// ally there (F-0804).
		scn.MoveCardsToBottomOfDeck(scn.GetFreepsCard("dain"));
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth's start-of-regroup offer (an ally from the draw deck), declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(dori, thrush);
		scn.AttachCardsTo(thorin, ori, axe);
		scn.MoveMinionsToTable(azog, alfrid, smaug);
		assertInZone(Zone.SUPPORT, dain, dori, thrush);
		assertAttachedTo(ori, thorin);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid, smaug);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();

		// A [dwarven] follower in the support area and one that Thorin bears; not a follower of another culture,
		// nor a [dwarven] card that is no follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(dori, ori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush, axe));
		scn.FreepsChooseCard(ori);
		assertInZone(Zone.DISCARD, ori);
		assertInZone(Zone.SUPPORT, dori, thrush);
		assertAttachedTo(axe, thorin);

		// Minions of two cultures and races; not Smaug.
		assertTrue(scn.FreepsHasCardChoiceAvailable(azog, alfrid));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smaug));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(azog);
		assertInZone(Zone.DISCARD, azog);
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid, smaug);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSmaugTheOnlyMinionTheFollowerIsDiscardedAndNoMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		// Dáin, a [dwarven] ally, laid in the draw deck: Esgaroth's start-of-regroup offer is made only with such an
		// ally there (F-0804).
		scn.MoveCardsToBottomOfDeck(scn.GetFreepsCard("dain"));
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(dori);
		scn.MoveMinionsToTable(smaug);
		assertInZone(Zone.SUPPORT, dain, dori);
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, dori);
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var azog = scn.GetShadowCard("azog");
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(dori);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, dain, dori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		assertInZone(Zone.SUPPORT, dain, dori);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, dori);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var thorin = scn.GetFreepsCard("thorin");
		var thrush = scn.GetFreepsCard("thrush");
		var axe = scn.GetFreepsCard("axe");
		var azog = scn.GetShadowCard("azog");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(thrush);
		scn.AttachCardsTo(thorin, axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, dain, thrush);
		assertAttachedTo(axe, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		assertInZone(Zone.SUPPORT, dain, thrush);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, thrush);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
	}

	@Test
	public void NoOfferAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var thorin = scn.GetFreepsCard("thorin");
		var dori = scn.GetFreepsCard("dori");
		var azog = scn.GetShadowCard("azog");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.AttachCardsTo(thorin, dori);
		assertAttachedTo(dori, thorin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);

		scn.SkipToPhase(Phase.MANEUVER);
		// Dori's Aid (exert Bilbo) is offered as the phase starts, though he is borne already; Dain's discard is not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, azog);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainAtTheSecondRegroupPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dain = scn.GetFreepsCard("dain");
		var dori = scn.GetFreepsCard("dori");
		var ori = scn.GetFreepsCard("ori");
		var azog = scn.GetShadowCard("azog");
		var alfrid = scn.GetShadowCard("alfrid");
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(dori, ori);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, dain, dori, ori);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, alfrid);
		assertInZone(Zone.SUPPORT, dain, dori, ori);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, alfrid);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(ori);
		scn.FreepsChooseCard(azog);
		assertInZone(Zone.DISCARD, ori, azog);

		// The fellowship moves on to Rivendell (site 3); Alfrid stays in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dain));
		scn.FreepsAcceptOptionalTrigger();
		// Dori, the one [dwarven] follower left, and Alfrid, the one minion, go unasked.
		assertInZone(Zone.DISCARD, dori, alfrid);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
