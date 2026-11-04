package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_003_Tests
{

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an [isengard]
	// minion, and none is laid).  Site 3, Rivendell Valley 1_341, and site 5, The Bridge of Khazad-dum 1_349, are none.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_3");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 while skirmishing an Orc
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion; "To play, spot a Dwarf" is its whole text
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an
	// [isengard] minion).  Site 3, the home of Thrarin (3F), is Rivendell Valley 1_341, a sanctuary and no
	// mountain.  No Dwarf ally has a home that is a mountain: each is of site 3 of the Fellowship path.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_3");
					put("axe2", "11_3");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeofKhazaddumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Axe of Khazad-dûm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Dwarf.
		 * While bearer is at a mountain site, he is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Axe of Khazad-dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnADwarfCompanionOrADwarfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInHand(axe);

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(thrarin);
		assertAttachedTo(axe, thrarin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnlyWhileTheFellowshipIsAtAMountainSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var farin = scn.GetFreepsCard("farin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(farin, guard);
		scn.AttachCardsTo(farin, axe);

		scn.StartGame();
		assertAttachedTo(axe, farin);
		scn.SkipToSite(3);

		// Rivendell Valley is no mountain.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// The move to Pass of Caradhras, a mountain: the bearer is damage +1, the other Dwarf is not.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// On to The Bridge of Khazad-dum, no mountain: the bonus is gone.
		scn.SkipToSite(5);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
	}

	@Test
	public void BorneByAnAllyWhoseHomeIsNoMountainItGivesNothingWithTheFellowshipAtAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(farin, axe);
		scn.AttachCardsTo(thrarin, axe2);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));

		// The companion is with the fellowship, at the mountain.  Thrarin is at his home site, Rivendell Valley
		// (CR 4.2 "ally").
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
	}
}
