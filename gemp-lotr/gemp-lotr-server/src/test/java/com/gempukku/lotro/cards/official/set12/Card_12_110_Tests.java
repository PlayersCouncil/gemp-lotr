package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_110_Tests
{

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains) as site 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cleaving", "12_110");
					put("cleaving2", "12_110");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, resistance 6; "Valiant." is his whole text
					put("chain", "4_278");      // Heavy Chain: [rohan] armor on a Man; an overwhelm bar only
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion, strength 3, resistance 6; his abilities are used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; "Knight." is his whole text
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a regroup ability used only on purpose (cheated in)

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10; fierce and damage +1 only at 2 burdens
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground, Plains; exerts each Uruk-hai PLAYED, and none is played here
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Towers path: site 2 is Eastfold 4_331, a plains and no battleground; its Shadow action needs 3
	// [isengard] trackers.
	protected VirtualTableScenario GetEastfoldScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cleaving", "12_110");
					put("cleaving2", "12_110");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, resistance 6
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon, strength +2; damage +1 only against an Uruk-hai
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion, strength 3, resistance 6
					put("orcarmor", "10_113");  // Orc Armor: [shire] armor on a Hobbit; each Orc and Uruk-hai skirmishing bearer is strength -1

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10; fierce and damage +1 at 2 burdens
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path: site 2 is King's Tent 7_335, no plains; its Shadow action is used only on purpose.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cleaving", "12_110");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, resistance 6
					put("mail", "1_101");       // Coat of Mail: [gondor] armor on a Man; an overwhelm bar only

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CleavingaPathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Cleaving a Path
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a [rohan] companion strength +1 for each of the following that is true: he or she is at a plains site; he or she has resistance 4 or more; he or she is bearing armor; he or she is skirmishing a wounded minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cleaving");

		assertEquals("Cleaving a Path", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AllFourAreTrueForPlus4OnlyInTheSkirmishItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cleaving = scn.GetFreepsCard("cleaving");
		var cleaving2 = scn.GetFreepsCard("cleaving2");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard, merry, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, chain);
		scn.MoveCardsToSupportArea(cleaving, cleaving2);

		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(guard, chain.getAttachedTo());
		assertInZone(Zone.SUPPORT, cleaving, cleaving2);

		scn.MoveMinionsToTable(savage, nelya);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cleaving));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{knight, nelya});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsUseCardAction(cleaving);
		assertInZone(Zone.DISCARD, cleaving);
		assertInZone(Zone.SUPPORT, cleaving2);

		// [rohan] companions, Man and Hobbit; not the [gondor] Knight or the [rohan] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// 6, and 1 each for the plains, resistance 6, the armor and the wounded minion
		assertEquals(10, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(knight);

		// The next skirmish is a phase of its own: the bonus is gone.
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ResistanceOfExactly4CountsAndAHandWeaponAnUnwoundedMinionAndAWaitingSkirmishDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEastfoldScenario();

		var cleaving = scn.GetFreepsCard("cleaving");
		var cleaving2 = scn.GetFreepsCard("cleaving2");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		scn.MoveCompanionsToTable(guard, merry);
		scn.AttachCardsTo(guard, sword);
		scn.AttachCardsTo(merry, orcarmor);
		scn.MoveCardsToSupportArea(cleaving, cleaving2);

		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(guard, sword.getAttachedTo());
		assertEquals(merry, orcarmor.getAttachedTo());
		assertInZone(Zone.SUPPORT, cleaving, cleaving2);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(savage, nelya);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, nelya}, new PhysicalCardImpl[]{merry, savage});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		// 6, less 2 burdens
		assertEquals(4, scn.GetResistance(guard));
		assertEquals(4, scn.GetResistance(merry));
		// 6, and 2 for the Sword
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(merry));

		// The Guard bears a hand weapon and skirmishes an unwounded minion: the plains and his resistance count.
		scn.FreepsUseCardAction(cleaving);
		scn.FreepsChooseCard(guard);
		assertEquals(10, scn.GetStrength(guard));

		// Merry bears armor of another culture and is assigned to the wounded Savage, whose skirmish has not begun:
		// the plains, his resistance and the armor count.
		scn.ShadowPass();
		scn.FreepsUseCardAction(cleaving2);
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AwayFromAPlainsArmorAndAWoundedMinionCountAndResistanceOf3DoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var cleaving = scn.GetFreepsCard("cleaving");
		var guard = scn.GetFreepsCard("guard");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mail);
		scn.MoveCardsToSupportArea(cleaving);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(guard, mail.getAttachedTo());
		assertInZone(Zone.SUPPORT, cleaving);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());

		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		// 6, less 3 burdens
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(6, scn.GetStrength(guard));

		// The Guard is the one [rohan] companion, and is chosen unasked.
		scn.FreepsUseCardAction(cleaving);

		// 6, and 1 each for the armor and the wounded minion
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
