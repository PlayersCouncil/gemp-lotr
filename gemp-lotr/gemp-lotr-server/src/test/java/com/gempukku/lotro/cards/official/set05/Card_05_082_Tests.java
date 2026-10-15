package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamling", "5_82");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; valiant only
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a Regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a Maneuver ability used only on purpose

					put("armor", "7_245");      // Riding Armor: [rohan] possession, armor, bearer must be a [rohan] Man; changes no number, a Response used only on purpose
					put("armor2", "7_245");
					put("helm", "5_89");        // Rohirrim Helm: [rohan] possession, helm, bearer must be a [rohan] Man; changes no number, a Skirmish ability used only on purpose
					put("gondorarmor", "1_92"); // Armor: a [gondor] possession, bearer must be a Man
					put("brought", "19_24");    // Brought Down from Inside: a [rohan] CONDITION, bearer must be a [rohan] Man

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, strength 5; damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GamlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gamling, Warrior of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>. To play, spot a [rohan] Man.<br><b>Skirmish:</b> Play a [rohan] possession on Gamling to make him strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gamling");

		assertEquals("Gamling", card.getBlueprint().getTitle());
		assertEquals("Warrior of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [rohan] companion who is no Man (Merry, with fewer than 3 [rohan] companions), and a Man of [gondor].
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(gamling, guard);

		scn.StartGame();
		assertInHand(gamling, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gamling));

		scn.FreepsPlayCard(guard);
		assertInPlay(guard);
		assertTrue(scn.FreepsPlayAvailable(gamling));

		scn.FreepsPlayCard(gamling);
		assertInPlay(gamling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBesideARohanManWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(gamling);

		scn.StartGame();
		assertInHand(gamling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gamling));

		scn.FreepsPlayCard(gamling);
		assertInPlay(gamling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityPlaysARohanPossessionOnHimToMakeHimStrengthPlus2AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		var gondorarmor = scn.GetFreepsCard("gondorarmor");
		var brought = scn.GetFreepsCard("brought");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gamling, guard);
		scn.MoveCardsToHand(armor, helm, gondorarmor, brought);

		scn.StartGame();
		assertInHand(armor, helm, gondorarmor, brought);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gamling, savage);
		scn.FreepsResolveSkirmish(gamling);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(armor, helm, gondorarmor, brought);
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(0, scn.GetKeywordCount(gamling, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(gamling));

		scn.FreepsUseCardAction(gamling);
		// [rohan] possessions of two classes; not a [rohan] condition, not a possession of [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, helm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gondorarmor, brought));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(armor);

		// The armor goes on Gamling, not on the Guard, who could bear it too.
		assertAttachedTo(armor, gamling);
		assertTrue(scn.GetAttachedCards(guard).isEmpty());
		assertInHand(helm, gondorarmor, brought);
		// Riding Armor changes no number: 6 + 2.
		assertEquals(8, scn.GetStrength(gamling));
		assertEquals(1, scn.GetKeywordCount(gamling, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithoutARohanPossessionHeCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gamling);
		scn.AttachCardsTo(gamling, armor);
		// A [rohan] possession in hand, of a class he already bears: one armor to a bearer.
		scn.MoveCardsToHand(armor2);

		scn.StartGame();
		assertAttachedTo(armor, gamling);
		assertInHand(armor2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gamling, savage);
		scn.FreepsResolveSkirmish(gamling);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gamling));
		assertInHand(armor2);
		assertEquals(6, scn.GetStrength(gamling));
	}

	@Test
	public void TheBonusLastsUntilTheEndOfHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(gamling, guard);
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		assertInHand(armor);
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gamling, savage}, new PhysicalCardImpl[]{guard, savage2});
		scn.FreepsResolveSkirmish(gamling);

		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(gamling);
		assertAttachedTo(armor, gamling);
		scn.ShadowPass();

		// Just before the boundary: the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gamling));
		assertEquals(1, scn.GetKeywordCount(gamling, Keyword.DAMAGE));

		scn.FreepsPass();
		scn.ShadowPass();
		// He beats the Savage (8 against 5), which takes 1 wound for the loss + 1 for his damage +1; the Guard's
		// skirmish is the other one left.
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(guard);

		// Just after: the next skirmish is a phase of its own, and the bonus is gone; the armor stays.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertAttachedTo(armor, gamling);
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(0, scn.GetKeywordCount(gamling, Keyword.DAMAGE));
	}
}
