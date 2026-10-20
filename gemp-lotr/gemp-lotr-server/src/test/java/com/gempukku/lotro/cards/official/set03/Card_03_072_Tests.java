package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapped", "3_72");

					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, damage +1 only
					put("scout", "1_191");      // Moria Scout: a [moria] Orc, strength 6; a trigger on its own play only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only
					put("anduril", "7_79");     // Andúril, Flame of the West: an ARTIFACT hand weapon on Aragorn, damage +1; its site action is used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; exerts to take part in skirmishes
					put("battleaxe", "7_9");    // Gimli's Battle Axe, Trusted Weapon: a hand weapon any Dwarf may bear, damage +1 while a threat can be spotted
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1 of his own; a skirmish ability only
					put("axe", "4_41");         // Axe of Erebor: a hand weapon on Gimli, damage +1; a skirmish ability only
					put("lord", "1_21");        // Lord of Moria: a [dwarven] condition, "Each Dwarf is damage +1"
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength 6 and +1 for each race in the fellowship
					put("glamdring", "1_75");   // Glamdring: a hand weapon on Gandalf, damage +1; an ability used only on purpose
					put("staff", "4_91");       // Gandalf's Staff, Walking Stick: a staff, no weapon; Gandalf is damage +2 while 2 twilight can be spotted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrappedandAloneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Trapped and Alone
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each character skirmishing an [isengard] Orc loses all <b>damage</b> bonuses from weapons.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trapped");

		assertEquals("Trapped and Alone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionOrAnAllySkirmishingAnIsengardOrcLosesTheWeaponsDamageBonusForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var thrarin = scn.GetFreepsCard("thrarin");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(thrarin, battleaxe);

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, worker);
		// The Battle Axe's damage +1 wants a threat to spot.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.ATTACHED, anduril, battleaxe);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, servant}, new PhysicalCardImpl[]{thrarin, worker});
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(aragorn);
		// An artifact's damage bonus is lost in the skirmish; Thrarin is assigned to an [isengard] Orc too, but his
		// skirmish has not begun, and he keeps his.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		// An ally loses a possession's bonus as a companion does; Aragorn's skirmish is over, and his is back.
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyTheDamageBonusesThatComeFromWeaponsAreLost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var lord = scn.GetFreepsCard("lord");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.MoveCardsToSupportArea(lord);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(gandalf, glamdring, staff);

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, worker);
		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.ATTACHED, axe, glamdring, staff);

		scn.SkipToAssignments();
		// 2 for Ettenmoors + 3 companions: the Staff's 2 twilight tokens can be spotted.
		assertEquals(5, scn.GetTwilight());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, servant}, new PhysicalCardImpl[]{gandalf, worker});
		// Gimli: his own +1, Lord of Moria's +1, the Axe's +1.  Gandalf: the Staff's +2, Glamdring's +1.
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(gimli);
		// Only the Axe's goes: a condition's bonus and his own stay.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		assertEquals(5, scn.GetTwilight());
		scn.FreepsResolveSkirmish(gandalf);
		// Only Glamdring's goes: the Staff is an item and no weapon.
		assertEquals(2, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingIsLostSkirmishingAnIsengardUrukHaiOrAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(gimli, axe);

		var trapped = scn.GetShadowCard("trapped");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, scout);
		assertInZone(Zone.SUPPORT, trapped);
		assertInZone(Zone.ATTACHED, anduril, axe);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, scout});
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// An [isengard] minion who is no Orc.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// An Orc who is not [isengard].
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
