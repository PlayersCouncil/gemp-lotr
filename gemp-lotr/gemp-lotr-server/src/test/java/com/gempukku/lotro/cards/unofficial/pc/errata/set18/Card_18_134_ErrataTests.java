package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_134_ErrataTests
{
	// The card's own block: the Shadow player lays each next site, Doorway to Doom among them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion, vitality 4
					put("gaffer", "1_291");       // The Gaffer: an ally, vitality 2; a fellowship ability never used here
					put("smeagol", "5_28");       // Sméagol, Old Noser: spotted by One Good Turn Deserves Another
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("raiders", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only
					// There's Another Way: "Regroup: Discard this condition to replace the fellowship's
					// current site with a site from your adventure deck."
					put("anotherway1", "12_40");
					put("anotherway2", "12_40");
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck." Vitality 3.
					put("nelya", "11_222");
					// One Good Turn Deserves Another: "Spot Sméagol to play the fellowship's next site. Then
					// you may add a burden to take this card back into hand."
					put("goodturn1", "11_49");
					put("goodturn2", "11_49");
					put("goodturn3", "11_49");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");       // Sirannon Ruins: River, and no text
					put("doorway", "68_134");
					put("glade", "11_239");       // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("road", "11_236");        // East Road: each companion is twilight cost +2; never laid, and no companion is played
					// Never laid in these tests, and never named: five copies of Expanding Marshland.
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DoorwayToDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Doorway to Doom
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Mountain.</b> When the fellowship moves to this site, each player wounds 2 of their
		 * 	characters.  This site cannot be replaced.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("doorway");

		assertEquals("Doorway to Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void MovingToDoorwayTheFreePeoplesPlayerWoundsTwoOfHisThreeCharactersAndMovingOnFromItWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(gaffer);

		var doorway = scn.GetShadowSite("doorway");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gaffer));

		// The fellowship moves in the fellowship phase, and the Shadow player lays Doorway to Doom as site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(doorway);

		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// The Free Peoples player chooses exactly 2 of his 3 characters: the Ring-bearer, a companion and an ally.
		assertTrue(scn.FreepsDecisionAvailable("wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gaffer));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gaffer);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		// The Shadow player has no character and is asked for nothing but his Shadow actions.
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on in the regroup phase, to a site that does nothing.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(glade);

		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(doorway, scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToDoorwayInTheRegroupPhaseEachPlayerWoundsTwoOfHisOwnCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(gaffer);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");

		var doorway = scn.GetShadowSite("doorway");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);

		// Three minions that have come through the turn's skirmishes, of two cultures and two races.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(savage);
		scn.MoveMinionsToTable(raiders);

		// The fellowship moves again, and the Shadow player lays Doorway to Doom as site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(doorway);

		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());

		// The Free Peoples player chooses among his own characters only.
		assertTrue(scn.FreepsDecisionAvailable("wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gaffer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, savage, raiders));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, gaffer);

		// And the Shadow player among his.
		assertTrue(scn.ShadowDecisionAvailable("wound"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, gaffer));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, raiders);

		// One wound on each of the two chosen, none on the third.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APlayerWithOnlyOneCharacterWoundsThatOneOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		var doorway = scn.GetShadowSite("doorway");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(doorway);

		// Frodo is alone in the fellowship and the Goblin Patrol Troop alone on its side: nobody has a
		// choice, and each takes one wound, not two.
		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesCardPutsDoorwayUnderTheFellowshipWoundingNobodyAndASecondCannotReplaceIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var anotherway1 = scn.GetFreepsCard("anotherway1");
		var anotherway2 = scn.GetFreepsCard("anotherway2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(anotherway1, anotherway2);

		var doorway = scn.GetFreepsSite("doorway");
		var road = scn.GetFreepsSite("road");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(glade, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, doorway, road);

		// The first There's Another Way replaces Fangorn Glade, a site with no such text, with Doorway to Doom.
		assertTrue(scn.FreepsActionAvailable(anotherway1));
		scn.FreepsUseCardAction(anotherway1);
		scn.FreepsChooseCardBPFromSelection(doorway);

		assertInZone(Zone.DISCARD, anotherway1);
		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, glade);
		// The site came to the fellowship; the fellowship did not move to it. Nobody wounds anybody.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// The second pays its cost and replaces nothing.
		assertTrue(scn.FreepsActionAvailable(anotherway2));
		scn.FreepsUseCardAction(anotherway2);

		assertInZone(Zone.DISCARD, anotherway2);
		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(doorway, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, road);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DoorwayLyingAheadOfTheFellowshipDoesNotStopTheCurrentSiteBeingReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anotherway1 = scn.GetFreepsCard("anotherway1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(anotherway1);

		var doorway = scn.GetShadowSite("doorway");
		var road = scn.GetFreepsSite("road");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.SkipToPhase(Phase.REGROUP);

		// Doorway to Doom is on the path, site 3, ahead of the fellowship at Fangorn Glade, site 2.
		scn.MoveCardToAdventurePath(doorway);
		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(doorway, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, road);

		// The card's refusal is for Doorway itself: the site the fellowship stands on is another, and is replaced.
		assertTrue(scn.FreepsActionAvailable(anotherway1));
		scn.FreepsUseCardAction(anotherway1);
		scn.FreepsChooseCardBPFromSelection(road);

		assertInZone(Zone.DISCARD, anotherway1);
		assertEquals(road, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, glade);
		assertEquals(doorway, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AShadowCardPutsDoorwayUnderTheFellowshipWoundingNobodyAndCannotThenReplaceIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var nelya = scn.GetShadowCard("nelya");

		var doorway = scn.GetShadowSite("doorway");
		var road = scn.GetShadowSite("road");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.ADVENTURE_DECK, doorway, road);

		// Úlairë Nelya replaces Fangorn Glade, a site with no such text, with Doorway to Doom.
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(doorway);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, glade);
		// The site came to the fellowship; the fellowship did not move to it. Nobody wounds anybody: the
		// one wound on Nelya is her own exertion.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// She exerts again, and replaces nothing.
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);

		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(doorway, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, road);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DoorwayPlayedAsTheNextSiteWoundsNobodyAndIsNotReplacedByAnotherPlayOfTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var goodturn1 = scn.GetFreepsCard("goodturn1");
		var goodturn2 = scn.GetFreepsCard("goodturn2");
		var goodturn3 = scn.GetFreepsCard("goodturn3");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(goodturn1, goodturn2, goodturn3);

		var doorway = scn.GetFreepsSite("doorway");
		var road = scn.GetFreepsSite("road");
		var glade = scn.GetFreepsSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The first One Good Turn Deserves Another lays Fangorn Glade as site 2, ahead of the fellowship.
		scn.FreepsPlayCard(goodturn1);
		scn.FreepsChooseCardBPFromSelection(glade);
		scn.FreepsChooseNo();
		assertEquals(glade, scn.GetSite(2));

		// The second replaces it there with Doorway to Doom: a next site with no such text can be replaced.
		scn.FreepsPlayCard(goodturn2);
		scn.FreepsChooseCardBPFromSelection(doorway);
		scn.FreepsChooseNo();
		assertEquals(doorway, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, glade);
		// Nobody has moved to it.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));

		// The third finds no site it may replace, and goes straight on to its offer of a burden.
		assertTrue(scn.FreepsPlayAvailable(goodturn3));
		scn.FreepsPlayCard(goodturn3);
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();

		assertInZone(Zone.DISCARD, goodturn3);
		assertEquals(doorway, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, road, glade);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DoorwayWhereTheOtherPlayersFellowshipStandsIsNotReplacedByAPlayOfTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var goodturn1 = scn.GetFreepsCard("goodturn1");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(goodturn1);

		var doorway = scn.GetFreepsSite("doorway");
		var road = scn.GetFreepsSite("road");
		var glade = scn.GetShadowSite("glade");
		var otherFrodo = scn.GetShadowRingBearer();

		// Player 1 moves to site 2, Fangorn Glade, and stays there.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.SkipToOtherPlayersTurn();

		// Player 2 moves to site 2 and then on, and player 1 lays his own Doorway to Doom as site 3.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(otherFrodo));
		scn.ShadowChooseToMove();
		scn.FreepsChooseCardBPFromSelection(doorway);

		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// It is player 2's fellowship that moved there: his lone Ring-bearer takes a wound.
		assertEquals(1, scn.GetWoundsOn(otherFrodo));

		// Player 1's next turn: his fellowship is at site 2 and Doorway to Doom is the site ahead of it.
		scn.SkipToOtherPlayersTurn();
		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(doorway, scn.GetSite(3));

		// One Good Turn Deserves Another finds no site it may replace, and goes straight on to its offer
		// of a burden.
		assertTrue(scn.FreepsPlayAvailable(goodturn1));
		scn.FreepsPlayCard(goodturn1);
		assertTrue(scn.FreepsDecisionAvailable("Do you want to add a burden"));
		scn.FreepsChooseNo();

		assertInZone(Zone.DISCARD, goodturn1);
		assertEquals(doorway, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, road);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
