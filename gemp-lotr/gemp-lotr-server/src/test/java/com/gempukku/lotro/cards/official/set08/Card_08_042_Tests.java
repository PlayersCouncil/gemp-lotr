package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("path", "8_42");
					put("path2", "8_42");       // a second copy, to show which copy "here" is
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 to a [gondor] Man, so that the Merchant can exert
					// Oathbreaker: a [gondor] companion who is a Wraith, no Man; a skirmish ability used only on purpose
					put("oath", "8_41");
					put("gimli", "0_62");       // Gimli, Dwarven Delegate: [dwarven] Dwarf companion, vitality 3; damage +1 is its whole text
					put("household", "5_83");   // Household Guard: [rohan] Man companion, vitality 3; valiant is its whole text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability used only on purpose
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Strength Born of Fear: a Shadow support-area condition, no minion; a skirmish ability used only on purpose
					put("fear", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APathAppointedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: A Path Appointed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Exert a [gondor] Man and either a [dwarven] companion or a [rohan] companion to add a [gondor] token here.<br><b>Regroup:</b> Remove a [gondor] token here to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("path");

		assertEquals("A Path Appointed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionExertsAGondorManAndADwarvenOrRohanCompanionToAddATokenToThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		var path2 = scn.GetFreepsCard("path2");
		var gknight = scn.GetFreepsCard("gknight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var oath = scn.GetFreepsCard("oath");
		var gimli = scn.GetFreepsCard("gimli");
		var household = scn.GetFreepsCard("household");
		var grimir = scn.GetFreepsCard("grimir");
		var herdsman = scn.GetFreepsCard("herdsman");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(path, path2);
		scn.MoveCompanionsToTable(gknight, oath, gimli, household);
		scn.MoveAlliesToTable(merchant, grimir, herdsman);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		// The [gondor] Man: a companion and an ally; not a [gondor] companion who is no Man, nor a Man of [rohan].
		assertTrue(scn.FreepsHasCardChoiceAvailable(gknight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oath, household, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));

		// Then a [dwarven] or a [rohan] companion, both alternatives on offer at once; not an ally of either
		// culture, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, household));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, herdsman, frodo, oath, gknight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(household));
		assertEquals(0, scn.GetWoundsOn(gknight));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionAbleToExertARohanCompanionPaysAndThenTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		var gknight = scn.GetFreepsCard("gknight");
		var gimli = scn.GetFreepsCard("gimli");
		var household = scn.GetFreepsCard("household");
		var grimir = scn.GetFreepsCard("grimir");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCardsToSupportArea(path);
		scn.MoveCompanionsToTable(gknight, gimli, household);
		scn.MoveAlliesToTable(grimir, herdsman);

		scn.StartGame();
		// Gimli, vitality 3, exhausted; the Household Guard, vitality 3, can exert once more.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(household, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(household));
		assertEquals(0, scn.GetWoundsOn(gknight));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		// The Knight is the only [gondor] Man, and the [rohan] alternative the only one that can be paid: both exert
		// with nothing asked.
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertEquals(2, scn.GetWoundsOn(household));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// Now neither a [dwarven] nor a [rohan] companion can exert, though the Knight can, and so can Grimir and
		// the Herdsman, allies of those cultures.
		assertFalse(scn.FreepsActionAvailable(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAbleToExertADwarvenCompanionPaysAndAnExhaustedGondorManStopsTheAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		var gknight = scn.GetFreepsCard("gknight");
		var oath = scn.GetFreepsCard("oath");
		var gimli = scn.GetFreepsCard("gimli");
		var household = scn.GetFreepsCard("household");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCardsToSupportArea(path);
		scn.MoveCompanionsToTable(gknight, oath, gimli, household);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		// The Household Guard, vitality 3, exhausted; the Knight, vitality 3, can exert once more.
		scn.AddWoundsToChar(household, 2);
		scn.AddWoundsToChar(gknight, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(oath));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		// The [dwarven] alternative is the only one that can be paid, and is.
		assertEquals(2, scn.GetWoundsOn(gknight));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// The Knight, the one [gondor] Man, is now exhausted: Gimli can still exert, and so can Oathbreaker (a
		// [gondor] companion who is no Man) and the Herdsman (a Man of [rohan]).
		assertFalse(scn.FreepsActionAvailable(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionRemovesATokenFromThisCopyToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		var path2 = scn.GetFreepsCard("path2");
		scn.MoveCardsToSupportArea(path, path2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		// Each copy has gained one token by its fellowship ability in an earlier turn.
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(path2, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(path2));
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		// Minions of two cultures and races; not a Shadow card that is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(path2));
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, fear);

		scn.ShadowPass();
		// This copy has no token left and is not offered; the second copy, which has one, is.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(path));
		assertTrue(scn.FreepsActionAvailable(path2));
	}

	@Test
	public void WithNoMinionInPlayTheRegroupActionRemovesATokenForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		scn.MoveCardsToSupportArea(path);

		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		// No minion: the game goes straight from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		// The removal is a cost, so the action is offered with no minion to discard.
		assertTrue(scn.FreepsActionAvailable(path));
		scn.FreepsUseCardAction(path);

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
