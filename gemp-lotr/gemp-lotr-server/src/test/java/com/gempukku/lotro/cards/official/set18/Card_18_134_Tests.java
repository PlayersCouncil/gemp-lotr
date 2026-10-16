package com.gempukku.lotro.cards.official.set18;

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

public class Card_18_134_Tests
{
	// The card's own block: the Shadow player lays each next site, Doorway to Doom among them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion, vitality 4
					put("gaffer", "1_291");       // The Gaffer: an ally, vitality 2; a fellowship ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("raiders", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck." Vitality 3.
					put("nelya", "11_222");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");       // Sirannon Ruins: River, and no text
					put("doorway", "18_134");
					put("glade", "11_239");       // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("road", "11_236");        // East Road: each companion is twilight cost +2; never laid
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
		 * Game Text: <b>Mountain</b>. When the fellowship moves to this site, each player wounds two of his or
		 * 	her characters.
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
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));

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
	public void DoorwayPutUnderTheFellowshipByAReplacementWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var nelya = scn.GetShadowCard("nelya");

		var doorway = scn.GetShadowSite("doorway");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));

		// Úlairë Nelya replaces Fangorn Glade, under the fellowship, with Doorway to Doom.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(doorway);

		assertEquals(doorway, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, glade);
		// The site came to the fellowship; the fellowship did not move to it. Nobody wounds anybody: the one
		// wound on Nelya is her own exertion.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
