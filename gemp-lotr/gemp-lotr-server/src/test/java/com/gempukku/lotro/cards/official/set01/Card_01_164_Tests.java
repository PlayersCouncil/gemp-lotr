package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "1_164");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, Archer and nothing else
					put("bowman", "2_60");      // Goblin Bowman: [moria] Orc, Archer and nothing else
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc who is no archer; a "when you play" add (2), harmless cheated in
					put("assassin", "1_131");   // Orthanc Assassin: an [isengard] Uruk-hai archer; Archer and Damage +1 only

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home 6; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BitterHatredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bitter Hatred
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot a [moria] archer to wound an Elf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hatred");

		assertEquals("Bitter Hatred", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAMoriaArcherWoundsAnElfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var marksman = scn.GetShadowCard("marksman");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, aragorn, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		// Legolas (vitality 3) is exhausted: a wound can still be put on him, where an exertion could not.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.ShadowPlayAvailable(hatred));
		scn.ShadowPlayCard(hatred);

		// An Elf, companion or ally, exhausted or not; not a Man, a Dwarf or a Hobbit.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, gimli, frodo, marksman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(orophin);

		// One wound, on the Elf chosen.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(hatred);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AGoblinBowmanIsAMoriaArcherToSpotAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var bowman = scn.GetShadowCard("bowman");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(bowman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.ShadowPlayAvailable(hatred));
		scn.ShadowPlayCard(hatred);

		// Legolas is the one Elf, and is wounded with nothing asked.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(hatred);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAMoriaArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		var assassin = scn.GetShadowCard("assassin");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(hatred);

		scn.StartGame();
		// A [moria] Orc who is no archer, and archers of [isengard] and [elven].
		scn.MoveMinionsToTable(runner, assassin);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hatred));
	}

	@Test
	public void WithNoElfInPlayTheEventIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(hatred));
		scn.ShadowPlayCard(hatred);

		// No Elf to wound: nobody is wounded.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(hatred);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
