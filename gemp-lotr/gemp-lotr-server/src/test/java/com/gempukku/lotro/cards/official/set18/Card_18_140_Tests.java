package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_140_Tests
{
	// A Fellowship-block path with Streets of Bree in each adventure deck in place of site 9, which no test reaches.
	// It has no number: MoveCardToAdventurePath lays the Shadow player's copy as the site ahead.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pallando, Far-travelling One: [gandalf] follower, strength +1, Aid - (2).  Transferred to a
					// [gandalf] companion his own text asks nothing, so Gandalf is who bears him.
					put("pallando", "13_37");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					// Saruman, Servant of Sauron: [isengard] Shadow follower, strength +2, Aid - (1), removed from
					// the pool, to a minion.  His other text needs his bearer to win a skirmish.
					put("saruman", "17_38");
					put("troop", "1_177");      // Goblin Patrol Troop: no text at all; a maneuver phase, and a bearer for Saruman
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("streets", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path, for the cards that are not transferred to a character by an Aid.
	protected VirtualTableScenario GetOtherTransfersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pallando", "13_37");
					// Sam, Bearer of Great Need: "Regroup: Exert Sam and transfer a follower he is bearing to your
					// support area to discard a minion from play."
					put("sam", "13_156");
					put("troop", "1_177");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; his skirmish ability is not used
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 against an Orc only
					// Dwarven Axe: [dwarven] possession, cost 0, "Bearer must be a Dwarf"; its trigger needs a
					// skirmish won by its bearer
					put("axe", "1_9");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("streets", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StreetsofBreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Streets of Bree
		 * Unique: False
		 * Side:
		 * Culture:
		 * Shadow Number: 0
		 * Type: Site
		 * Subtype:
		 * Site Number: *
		 * Block: Shadows
		 * Direction: Left
		 * Game Text: <b>Dwelling</b>. While the fellowship is at this site, followers cannot be transferred to a character.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("streets");

		assertEquals("Streets of Bree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtStreetsOfBreeEachPlayersFollowerAidIsFlaggedAndItsCostTransfersNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(pallando);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, pallando, saruman);
		scn.MoveCardToAdventurePath(streets);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(streets, scn.GetCurrentSite());
		assertInZone(Zone.SUPPORT, pallando, saruman);

		// The Free Peoples follower's Aid is offered, says it will do nothing, and is paid for nothing.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertEquals("effect", scn.FreepsActionNoEffect(pallando));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pallando);
		assertNotAttachedTo(pallando, gandalf);

		// So is the Shadow player's: it removes (1), and Saruman goes to no minion.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals("effect", scn.ShadowActionNoEffect(saruman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(pool + 2 - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, saruman);
		assertNotAttachedTo(saruman, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftStreetsOfBreeEachPlayersFollowerIsTransferredByItsAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(pallando);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, pallando, saruman);
		scn.MoveCardToAdventurePath(streets);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(streets, scn.GetCurrentSite());
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, pallando, saruman);

		// Nobody is assigned to the Troop, and the fellowship moves on in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNotSame(streets, scn.GetCurrentSite());

		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertEquals("false", scn.FreepsActionNoEffect(pallando));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		scn.FreepsChooseCard(gandalf);

		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(pallando, gandalf);

		// The Troop is his only minion: Saruman goes to it unasked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals("false", scn.ShadowActionNoEffect(saruman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(pool + 2 - 1, scn.GetTwilight());
		assertAttachedTo(saruman, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtStreetsOfBreeAFollowerIsStillTransferredToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherTransfersScenario();

		var sam = scn.GetFreepsCard("sam");
		var pallando = scn.GetFreepsCard("pallando");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, pallando);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(pallando, sam);
		scn.MoveCardToAdventurePath(streets);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(streets, scn.GetCurrentSite());
		assertAttachedTo(pallando, sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));

		// "To a character" is the bar's: a follower moved from its bearer to the support area is no such transfer.
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.SUPPORT, pallando);
		assertNotAttachedTo(pallando, sam);
		assertInDiscard(troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtStreetsOfBreeAPossessionIsStillTransferredToACharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherTransfersScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var axe = scn.GetFreepsCard("axe");
		var streets = scn.GetShadowSite("streets");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		scn.MoveCardToAdventurePath(streets);
		scn.SkipToSite(2);

		// The fellowship phase of the Free Peoples player's next turn, at Streets of Bree.
		assertSame(streets, scn.GetCurrentSite());
		assertAttachedTo(axe, gimli);
		assertTrue(scn.FreepsTransferAvailable(axe));

		// Farin is the only other Dwarf: the Axe goes to him unasked.
		scn.FreepsTransferCard(axe);

		assertAttachedTo(axe, farin);
		assertNotAttachedTo(axe, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
