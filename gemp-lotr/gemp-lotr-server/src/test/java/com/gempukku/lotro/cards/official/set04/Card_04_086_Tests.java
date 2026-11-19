package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thonnas", "4_86");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability used only on purpose
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("guardian", "4_76");    // Lorien Guardian: Elf companion, strength 5; a regroup ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer; an archery ability used only on purpose
					put("arwensbow", "18_4");   // Arwen's Bow: [elven] ranged weapon, strength +1, no archer; its ability works only for Arwen
					put("longbow", "6_21");     // Naith Longbow: [elven] ranged weapon, strength +1, no archer; an archery ability used only on purpose
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, strength +1; a skirmish ability used only on purpose
					put("dinendal", "15_12");   // Dinendal, Mirkwood Archer: [elven] follower, "Bearer is an archer"; its exertion is only for a companion who is not [elven]
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThonnasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Thónnas, Naith Captain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.<br>While Thónnas bears a ranged weapon, each minion skirmishing him is strength -2 and Thónnas does not add to the fellowship archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thonnas");

		assertEquals("Thónnas", card.getBlueprint().getTitle());
		assertEquals("Naith Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndAnElfAllyIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(thonnas, celeborn);

		scn.StartGame();

		// A companion who is no Elf and an [elven] card that is no Elf in play, and no Elf.
		assertInHand(thonnas, celeborn);
		assertFalse(scn.FreepsPlayAvailable(thonnas));
		scn.FreepsPlayCard(celeborn);

		assertTrue(scn.FreepsPlayAvailable(thonnas));
		scn.FreepsPlayCard(thonnas);
		assertInZone(Zone.FREE_CHARACTERS, thonnas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBySpottingAnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(guardian);
		scn.MoveCardsToHand(thonnas);

		scn.StartGame();

		assertInHand(thonnas);
		assertTrue(scn.FreepsPlayAvailable(thonnas));
		scn.FreepsPlayCard(thonnas);
		assertInZone(Zone.FREE_CHARACTERS, thonnas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileHeBearsARangedWeaponEachMinionSkirmishingThonnasIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var guardian = scn.GetFreepsCard("guardian");
		var arwensbow = scn.GetFreepsCard("arwensbow");
		var longbow = scn.GetFreepsCard("longbow");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(thonnas, guardian);
		// A hand weapon on him; a ranged weapon on the Guardian, which makes no archer.
		scn.AttachCardsTo(thonnas, sword);
		scn.AttachCardsTo(guardian, longbow);
		scn.MoveCardsToHand(arwensbow);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(sword, thonnas);
		assertAttachedTo(longbow, guardian);
		// The ranged weapon goes on him by legal play: the Guardian bears one already, so Thónnas is the only
		// bearer there is.
		scn.FreepsPlayCard(arwensbow);
		assertAttachedTo(arwensbow, thonnas);

		scn.MoveMinionsToTable(troop, troop2, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thonnas, troop},
				new PhysicalCardImpl[]{guardian, troop2});
		// The Free Peoples player may put only one minion on each; the Shadow player adds the second.
		scn.ShadowAssignToMinions(thonnas, savage);

		// The Guardian's skirmish first.  The Guardian bears a ranged weapon too, and the two minions assigned
		// to Thónnas are not skirmishing him yet: none of them is weakened.
		scn.FreepsResolveSkirmish(guardian);
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		scn.PassSkirmishActions();

		// Both minions skirmishing him, of two cultures and races, are -2.
		scn.FreepsResolveSkirmish(thonnas);
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThonnasDoesNotAddToTheFellowshipArcheryTotalOnceHeBearsARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var legolas = scn.GetFreepsCard("legolas");
		var dinendal = scn.GetFreepsCard("dinendal");
		var longbow = scn.GetFreepsCard("longbow");
		var arwensbow = scn.GetFreepsCard("arwensbow");
		scn.MoveCompanionsToTable(thonnas, legolas);
		// An archer before he bears a ranged weapon, by a follower; the bow played later gives no archer.
		scn.AttachCardsTo(thonnas, dinendal);
		// Legolas bears a ranged weapon too, and is an archer: he must go on adding to the total.
		scn.AttachCardsTo(legolas, arwensbow);
		scn.MoveCardsToHand(longbow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertAttachedTo(dinendal, thonnas);
		assertAttachedTo(arwensbow, legolas);
		// Thónnas and Legolas.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		// Legolas bears a ranged weapon already, so Thónnas is the only bearer there is.
		scn.FreepsPlayCard(longbow);
		assertAttachedTo(longbow, thonnas);
		assertTrue(scn.HasKeyword(thonnas, Keyword.ARCHER));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		// Legolas alone, though he bears a ranged weapon as well.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void BearingOnlyAHandWeaponThonnasWeakensNoMinionAndAddsToTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var sword = scn.GetFreepsCard("sword");
		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveCompanionsToTable(thonnas);
		scn.AttachCardsTo(thonnas, sword, dinendal);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertAttachedTo(sword, thonnas);
		assertAttachedTo(dinendal, thonnas);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.PassArcheryActions();
		// The fellowship's one archery wound, on the only minion.
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(thonnas, troop);
		scn.FreepsResolveSkirmish(thonnas);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
