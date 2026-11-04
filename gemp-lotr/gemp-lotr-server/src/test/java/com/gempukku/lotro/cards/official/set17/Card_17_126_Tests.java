package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "17_126");
					put("guard2", "17_126");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5; hunter 6 is its whole text
					put("destroyer", "17_124");  // White Hand Destroyer: [uruk-hai] Uruk-hai, fierce; hunter 3 for each site its owner controls, so none here
					put("headsman", "18_89");    // Orkish Headsman: an [orc] Orc, hunter 1; strength +3 for each companion in the dead pile

					put("knight", "5_35");       // Gondorian Knight: [gondor] Man, strength 6, vitality 3; knight and no other text
					put("elf", "17_7");          // Elven Guardian: a companion with hunter 1; its maneuver heal used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDeathAndGloryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "17_126");
					put("runner", "1_178");      // Goblin Runner: [moria] Orc, strength 5, vitality 1, no hunter; its play trigger does not fire when cheated in
					put("household", "5_83");    // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and no other text
					// For Death and Glory: while the Ring-bearer is assigned to a skirmish, each minion skirmishing a
					// [rohan] companion loses hunter and is unable to gain it.
					put("glory", "17_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo loses to a Goblin Runner and goes on.
				VirtualTableScenario.GreatRing
		);
	}

	protected VirtualTableScenario GetSarumanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "17_126");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					put("cirith", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 is its whole text
					// Saruman, Instigator of Insurrection: while 6 companions can be spotted, each minion is hunter 2.
					// Unassigned and left alone, his other texts do not come up.
					put("saruman", "17_37");

					put("knight", "5_35");       // Gondorian Knight: [gondor] Man, strength 6, vitality 3; knight and no other text
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("knight4", "5_35");
					put("knight5", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br><b>Skirmish:</b> Exert this minion to make an [uruk-hai] hunter minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("guard");

		assertEquals("White Hand Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsItToMakeAnUrukHaiHunterMinionStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var destroyer = scn.GetShadowCard("destroyer");
		var headsman = scn.GetShadowCard("headsman");
		var knight = scn.GetFreepsCard("knight");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(knight, elf);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2, tracking, butcher, destroyer, headsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, guard}, new PhysicalCardImpl[]{elf, tracking});
		// The others are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(14, scn.GetStrength(tracking));
		assertTrue(scn.ShadowActionAvailable(guard));
		scn.ShadowUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));
		// Itself in the skirmish, an Uruk-hai assigned elsewhere and two unassigned, whatever their hunter number.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, guard2, tracking, butcher));
		// An [uruk-hai] minion with no hunter, a hunter minion of another culture, a hunter companion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(destroyer, headsman, elf, knight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracking);

		assertEquals(16, scn.GetStrength(tracking));
		assertEquals(0, scn.GetKeywordCount(destroyer, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The Tracking Uruk's own skirmish, against a hunter: no hunter bonus, and the +2 is gone.
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.IsCharSkirmishing(tracking));
		assertEquals(14, scn.GetStrength(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var tracking = scn.GetShadowCard("tracking");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, tracking);
		// Vitality 3: two wounds leave it exhausted.
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, guard);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(guard));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(14, scn.GetStrength(tracking));
	}

	@Test
	public void WithNoUrukHaiHunterMinionLeftItIsOfferedAndPaysForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeathAndGloryScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetShadowCard("guard");
		var runner = scn.GetShadowCard("runner");
		var household = scn.GetFreepsCard("household");
		var glory = scn.GetFreepsCard("glory");
		scn.MoveCompanionsToTable(household);
		scn.MoveCardsToSupportArea(glory);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{household, guard});
		// The Household Guard's skirmish first, with the Ring-bearer still assigned: the White Hand Guard has
		// lost hunter, and no other [uruk-hai] hunter minion is in play.
		scn.FreepsResolveSkirmish(household);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowActionAvailable(guard));
		scn.ShadowUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureWhoHasGainedHunterIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSarumanScenario();

		var guard = scn.GetShadowCard("guard");
		var tracking = scn.GetShadowCard("tracking");
		var cirith = scn.GetShadowCard("cirith");
		var saruman = scn.GetShadowCard("saruman");
		var knight = scn.GetFreepsCard("knight");
		// With the Ring-bearer, six companions.
		scn.MoveCompanionsToTable(knight, scn.GetFreepsCard("knight2"), scn.GetFreepsCard("knight3"),
				scn.GetFreepsCard("knight4"), scn.GetFreepsCard("knight5"));

		scn.StartGame();
		scn.MoveMinionsToTable(guard, tracking, cirith, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, guard);
		// The others are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();

		// Saruman makes every minion a hunter, the Sauron Uruk-hai included: an Uruk-hai that is not [uruk-hai].
		assertEquals(2, scn.GetKeywordCount(cirith, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(9, scn.GetStrength(cirith));
		assertTrue(scn.ShadowActionAvailable(guard));
		scn.ShadowUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, tracking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cirith));
		scn.ShadowChooseCard(tracking);

		assertEquals(16, scn.GetStrength(tracking));
		assertEquals(9, scn.GetStrength(cirith));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
