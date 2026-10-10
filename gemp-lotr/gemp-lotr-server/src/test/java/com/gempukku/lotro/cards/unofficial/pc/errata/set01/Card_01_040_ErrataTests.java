package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_040_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "51_40");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4
					put("radagast", "9_26");    // Radagast, The Brown: a [gandalf] Wizard companion who is not Gandalf
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, vitality 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, home site 6, vitality 3
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, home site 3, cost 1, vitality 2
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, home site 3, vitality 3
					put("deor", "7_222");       // Deor: [rohan] Man ally, home site 3 of the Tower block, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elrond, Lord of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 3
		 * Strength: 8
		 * Vitality: 4
		 * Game Text: To play, spot Gandalf or an Elf.
		 * At the start of each of your turns, heal up to 3 allies whose home is site 3.
		 * Fellowship: Exert Elrond twice (or once if you can spot 2 other [elven] allies) to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Lord of Rivendell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(radagast, aragorn);
		// A companion who is no Elf, and a [gandalf] Wizard of another name, who is not Gandalf.
		scn.MoveCardsToHand(elrond, gandalf);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		// 4 for Gandalf + 4 for Elrond
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Two companions in play, neither of them an Elf or Gandalf.
		scn.MoveCompanionsToTable(radagast, aragorn);
		scn.MoveCardsToHand(elrond, arwen);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		// 2 for Arwen + 4 for Elrond
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyLetsElrondBePlayedOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		// Silinde is an Elf and an ally, not a companion.
		scn.MoveCardsToHand(elrond, silinde);

		scn.StartGame();

		// An Elf in hand is not spotted.
		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(silinde);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		// 2 for Silinde + 4 for Elrond
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StartOfTurnHealsThreeChosenAlliesWhoseHomeIsSite3OnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var orophin = scn.GetFreepsCard("orophin");
		var deor = scn.GetFreepsCard("deor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(elrond, silinde, thrarin, bilbo, orophin, deor);
		scn.MoveCompanionsToTable(aragorn);

		// Four wounded allies whose home is site 3: an Elf (Elrond himself), another Elf, a Dwarf and a Hobbit.
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(bilbo, 2);
		// An ally whose home is site 6, an ally whose home is site 3 of the Tower block, and a companion.
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(aragorn, 1);

		// The first turn starts here.
		scn.StartGame();

		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, silinde, thrarin, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, deor, aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(bilbo));

		scn.FreepsChooseCards(elrond, thrarin, bilbo);
		// Healing removes one wound: Elrond and Bilbo each keep one of their two.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		// The fourth, not chosen
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChoosingNoAllyHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveAlliesToTable(elrond, silinde);
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(silinde, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, silinde));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCards();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsAgainAtTheStartOfItsOwnersNextTurnAndNotAtTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetFreepsCard("bilbo");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(elrond, bilbo, thrarin);
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(thrarin, 1);

		// The owner's first turn starts. One ally is chosen where three could be.
		scn.StartGame();
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, bilbo, thrarin));

		scn.FreepsChooseCards(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's turn starts: not one of "your turns". Nobody is asked and nobody is healed.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// The owner's second turn starts: asked again, and Bilbo is healed a second time.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, bilbo, thrarin));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(bilbo, elrond);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsElrondTwiceToDrawACardWithOnlyOneOtherElvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		var thrarin = scn.GetFreepsCard("thrarin");
		var arwen = scn.GetFreepsCard("arwen");
		// One other [elven] ally. Beside him: Elrond himself, an [elven] companion, and an ally of another
		// culture. None of those makes a second.
		scn.MoveAlliesToTable(elrond, silinde, thrarin);
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Vitality 4 with 2 wounds: he can exert once more but not twice.
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsElrondOnceToDrawACardWithTwoOtherElvenAllies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		// Exactly two other [elven] allies, of two homes: site 6 and site 3.
		scn.MoveAlliesToTable(elrond, orophin, silinde);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(elrond));

		// He exerts once, and is not asked which of the two prices to pay.
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetFreepsHandCount());
		// With 2 wounds he can still pay one exertion.
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
