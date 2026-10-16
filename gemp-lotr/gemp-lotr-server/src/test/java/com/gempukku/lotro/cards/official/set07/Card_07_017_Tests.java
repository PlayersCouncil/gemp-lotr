package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("asfaloth", "7_17");
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, strength 6; an archery ability only
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Elf ally, home site 6; an archery ability only
					put("haldir", "1_48");        // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1; harmless when cheated in
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him
					put("gollum", "7_59");        // Gollum, Vile Creature: [gollum] minion, strength 5; a skirmish ability used only on purpose
					// Controlled By The Ring: on Gollum. "Skirmish: If Gollum is not assigned to a skirmish, discard
					// this condition to have him replace a minion skirmishing a companion."
					put("controlled", "15_41");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("asfaloth", "7_17");
					// Arwen, Lady of Rivendell: the Arwen who is an ally, home site 3. Her texts are about Aragorn,
					// who is not on this table.
					put("arwen", "102_6");
					put("haldir", "1_48");        // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AsfalothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Asfaloth, Elven Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: Bearer must be an Elf.<br><b>Skirmish:</b> If bearer is Arwen, exert her to make each minion skirmishing her strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("asfaloth");

		assertEquals("Asfaloth", card.getBlueprint().getTitle());
		assertEquals("Elven Steed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(asfaloth);
		// An Elf companion and an Elf ally; not a Man or a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(orophin);
		assertAttachedTo(asfaloth, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingArwenMakesEachMinionSkirmishingHerStrengthMinus2UntilHerSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, asfaloth);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage},
				new PhysicalCardImpl[]{aragorn, chieftain});
		// The Goblin Runner is left over, and the Shadow player puts him on Arwen too.
		scn.ShadowAssignToMinions(arwen, runner);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.FreepsActionAvailable(asfaloth));

		scn.FreepsUseCardAction(asfaloth);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// Both minions skirmishing her, an Uruk-hai and an Orc of two cultures; not the Orc Chieftain, who is
		// assigned to Aragorn and whose skirmish has not begun.
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(chieftain));

		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(runner));

		// 6 + 2 for Asfaloth against 3 + 3: Arwen wins. The Runner dies of his wound and the Savage lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(runner);

		// In Aragorn's skirmish, the next, the Savage is his own 5 again.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));

		// Used in a skirmish that is not hers, it is paid for and the minion Aragorn is skirmishing is untouched.
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheBearerIsAnElfWhoIsNotArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(haldir, asfaloth);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{haldir, savage},
				new PhysicalCardImpl[]{arwen, runner});

		// Arwen is on the table and skirmishing, but Haldir is the one who bears Asfaloth.
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.IsCharSkirmishingAgainst(haldir, savage));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(asfaloth));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(asfaloth));
	}

	@Test
	public void NotOfferedWhileArwenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, asfaloth);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(arwen, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);

		// Vitality 3 with 2 wounds: she cannot exert.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, runner));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(asfaloth));
	}

	@Test
	public void BorneByTheArwenWhoIsAnAllyItIsOfferedAndSheExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveAlliesToTable(arwen);
		scn.AttachCardsTo(arwen, asfaloth);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haldir, savage);

		scn.FreepsResolveSkirmish(haldir);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(asfaloth));

		// She is in no skirmish, so no minion is skirmishing her: the exertion is paid and the Savage is untouched.
		scn.FreepsUseCardAction(asfaloth);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatComesIntoHerSkirmishAfterTheUseIsNotMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, asfaloth);

		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, gollum);
		scn.AttachCardsTo(gollum, controlled);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		// Gollum is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertFalse(scn.IsCharAssigned(gollum));
		scn.FreepsUseCardAction(asfaloth);
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(gollum));

		// Gollum takes the Savage's place in her skirmish.
		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, gollum));
		assertFalse(scn.IsCharSkirmishing(savage));

		// The minions it reached were the ones skirmishing her when it was used: Gollum, skirmishing her now, keeps
		// his 5, and the Savage keeps his -2 to the end of the skirmish.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(3, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
