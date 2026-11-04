package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "6_50");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; his text needs a weapon
					put("merry", "1_303");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a Skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a Fellowship ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a Skirmish ability used only on purpose

					// The cards that carry the tokens, each given tokens of its own culture as it is played (cheated in here):
					// Strands of Elven Hair ([elven] support possession) and Suspended Palaces ([dwarven] support condition)
					// have Fellowship abilities only; We Left None Alive ([rohan] support condition) a Skirmish ability
					// used only on purpose.
					put("strands", "9_22");
					put("palaces", "6_10");
					put("noneAlive", "6_97");

					put("brego", "13_63");      // Brego, Loyal Steed: [gondor] mount, bearer must be a [gondor] companion; a Maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: a possession that is no mount, bearer must be Aragorn; damage +1

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a Regroup ability used only on purpose
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a Regroup ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Aragorn, Defender of Free Peoples
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Skirmish:</b> Remove an [elven] token to make a minion skirmishing Aragorn strength -3.<br><b>Skirmish:</b> Remove a [dwarven] token to heal a Dwarf.<br><b>Regroup:</b> If Aragorn is mounted, remove a [rohan] token to wound a minion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Defender of Free Peoples", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void ElvenTokenMakesAMinionSkirmishingAragornStrengthMinus3UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var strands = scn.GetFreepsCard("strands");
		var worker = scn.GetShadowCard("worker");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(worker, sentry, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, worker}, new PhysicalCardImpl[]{merry, troop});
		scn.ShadowAssignToMinions(aragorn, sentry);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(aragorn, "strength -3"));
		// No [dwarven] token: the other Skirmish ability cannot be paid for.
		assertFalse(scn.FreepsActionAvailable(aragorn, "heal a Dwarf"));

		scn.FreepsUseCardAction(aragorn, "strength -3");
		assertEquals(1, scn.GetCultureTokensOn(strands));
		// The two minions skirmishing Aragorn, of two cultures and two races; not the Troop, assigned to Merry.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, sentry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(worker);

		assertEquals(2, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetStrength(worker));

		// 8 against 2 + 8: Aragorn loses, and takes 2 wounds for the Sentry's damage +1.  The minions live on.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, worker);

		// Each skirmish is a phase of its own: in Merry's, the Worker is back to his printed strength.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
	}

	@Test
	public void ElvenTokenAbilityUsedInAnotherSkirmishReachesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var strands = scn.GetFreepsCard("strands");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, merry, gimli);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.MoveMinionsToTable(worker, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, worker}, new PhysicalCardImpl[]{merry, troop});
		// Merry's skirmish first: the Worker is assigned to Aragorn, but not yet skirmishing him.
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetStrength(worker));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(aragorn, "strength -3"));
		// A Dwarf is wounded, but there is no [dwarven] token to pay with.
		assertFalse(scn.FreepsActionAvailable(aragorn, "heal a Dwarf"));

		// The ability has a cost, so it is offered: the token is paid, and no minion is skirmishing Aragorn.
		scn.FreepsUseCardAction(aragorn, "strength -3");
		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DwarvenTokenHealsADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var palaces = scn.GetFreepsCard("palaces");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(palaces);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 2);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.MoveMinionsToTable(worker);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, worker);

		assertTrue(scn.FreepsActionAvailable(aragorn, "heal a Dwarf"));
		// A minion is skirmishing Aragorn, but there is no [elven] token to pay with.
		assertFalse(scn.FreepsActionAvailable(aragorn, "strength -3"));

		scn.FreepsUseCardAction(aragorn, "heal a Dwarf");
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		// A Dwarf companion and a Dwarf ally; not Aragorn, wounded but no Dwarf.  No Shadow card is a Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhileMountedARohanTokenWoundsAMinionTwiceUntilNoneIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var brego = scn.GetFreepsCard("brego");
		var noneAlive = scn.GetFreepsCard("noneAlive");
		var strands = scn.GetFreepsCard("strands");
		var servant = scn.GetShadowCard("servant");
		var sentry = scn.GetShadowCard("sentry");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brego);
		scn.MoveCardsToSupportArea(noneAlive, strands);

		scn.StartGame();
		scn.AddTokensToCard(noneAlive, 2);
		scn.AddTokensToCard(strands, 1);
		assertAttachedTo(brego, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(noneAlive));
		assertEquals(1, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(servant, sentry, worker);
		// The Worker exhausted: 1 vitality left.
		scn.AddWoundsToChar(worker, 2);
		assertEquals(2, scn.GetWoundsOn(worker));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetCultureTokensOn(noneAlive));
		assertEquals(1, scn.GetCultureTokensOn(strands));
		// Minions of two cultures and two races, the exhausted one among them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(servant, sentry, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(servant);

		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(2, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Wounds, not exertions: the exhausted Worker can take them, and the first kills him.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(0, scn.GetCultureTokensOn(noneAlive));
		scn.FreepsChooseCard(worker);
		assertInDiscard(worker);
		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(sentry));

		// No [rohan] token is left (an [elven] one is): the ability is no longer offered.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertFalse(scn.FreepsActionAvailable(aragorn));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileAragornIsNotMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var brego = scn.GetFreepsCard("brego");
		var sword = scn.GetFreepsCard("sword");
		var noneAlive = scn.GetFreepsCard("noneAlive");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Aragorn bears a possession that is no mount; another companion is mounted.
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(boromir, brego);
		scn.MoveCardsToSupportArea(noneAlive);

		scn.StartGame();
		scn.AddTokensToCard(noneAlive, 2);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(brego, boromir);
		assertEquals(2, scn.GetCultureTokensOn(noneAlive));

		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(noneAlive));
		assertEquals(0, scn.GetWoundsOn(servant));
	}
}
