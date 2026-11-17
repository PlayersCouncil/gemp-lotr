package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_008_Tests
{

	// The Fellowship path: site 2 Ettenmoors (plains), site 3 Rivendell Valley (sanctuary), site 4 Balin's Tomb and
	// site 5 The Bridge of Khazad-dum (each underground and nothing else); site 1 Bag End has no terrain.  Mount Doom
	// 15_193 (Battleground, Mountain, Underground) is there to be laid as site 6 by MoveCardToAdventurePath; site 9
	// is never reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_8");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability, never used
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; to play, spot a Dwarf; stronger against an Orc only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion; a maneuver ability used only on purpose

					put("palaces", "6_10");     // Suspended Palaces: another [dwarven] condition holding [dwarven] tokens; a fellowship ability only
					put("stout", "4_57");       // Stout and Strong: [dwarven] support-area condition, cost 0, no text on its own play

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text answers only its own play
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("doom", "15_193");      // Mount Doom: Shadow number 3; sites in its region cannot be replaced
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain and nothing else (its Shadow ability
	// needs an [isengard] minion, and none is laid).  The Bridge of Khazad-dum, underground, is still site 5.
	protected VirtualTableScenario GetMountainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_8");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability, never used
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; to play, spot a Dwarf; stronger against an Orc only

					put("stout", "4_57");       // Stout and Strong: [dwarven] support-area condition, cost 0, no text on its own play
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");      // Pass of Caradhras: Mountain, Shadow number 2
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SubterraneanHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Subterranean Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert 2 Dwarves.<br>When you play this, add a [dwarven] token here for each mountain site and each underground site on the adventure path.<br><b>Skirmish:</b> Discard this from play or remove 2 tokens from here to make a Shadow player discard the top card of his or her draw deck.
		*/

		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");

		assertEquals("Subterranean Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertTrue(homestead.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, homestead.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(2, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsTwoDwarvesAndItTakesATokenForEachUndergroundSiteOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(palaces);
		scn.AddTokensToCard(palaces, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.SkipToSite(5);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(scn.GetSite(5), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);
		// Two Dwarf companions and a Dwarf ally; not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCards(gimli, grimir);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, homestead);
		// Balin's Tomb and the Bridge; not the plains, the sanctuary or Bag End.
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAMountainItTakesOneTokenAndNoneForASiteNotYetOnThePathNorForAnotherCardPlayedAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountainScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(homestead, stout);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.MOUNTAIN));
		// The Bridge of Khazad-dum, underground, is not yet on the path.
		assertNull(scn.GetSite(5));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));

		// The only two Dwarves exert unasked.
		scn.FreepsPlayCard(homestead);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		// Another [dwarven] condition played after it adds nothing here.
		scn.FreepsPlayCard(stout);
		assertInZone(Zone.SUPPORT, stout);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AControlledUndergroundSiteIsNoLongerOnThePathAndAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.SkipToSite(5);
		var tomb = scn.GetSite(4);
		scn.ShadowTakeControlOfSite(tomb);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		// Balin's Tomb is still underground, but a controlled site is in its controller's support area.
		assertTrue(scn.HasKeyword(tomb, Keyword.UNDERGROUND));
		assertInZone(Zone.SUPPORT, tomb);
		assertTrue(scn.HasKeyword(scn.GetSite(5), Keyword.UNDERGROUND));

		// The only two Dwarves exert unasked.
		scn.FreepsPlayCard(homestead);
		// The Bridge of Khazad-dum alone; not the controlled Balin's Tomb.
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Q-0383: A (Ketura, 2026-10-28): a site that is both a mountain and underground adds a token for each word.
	@Test
	public void ASiteThatIsBothAMountainAndUndergroundAddsATokenForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var doom = scn.GetShadowSite("doom");
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(doom);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertEquals(doom, scn.GetSite(6));
		assertTrue(scn.HasKeyword(doom, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(doom, Keyword.UNDERGROUND));

		scn.FreepsPlayCard(homestead);
		// Balin's Tomb 1 + the Bridge 1 + Mount Doom, a mountain site 1 and an underground site 1
		assertEquals(4, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableWithOnlyOneDwarfAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		// Gimli is exhausted: Farin is the one Dwarf who can exert.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInHand(homestead);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(homestead));
	}

	@Test
	public void SkirmishAbilityRemovesTwoTokensOrDiscardsItToDiscardTheTopCardOfTheShadowDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		var palaces = scn.GetFreepsCard("palaces");
		var freepsTop = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(homestead, palaces);
		scn.AddTokensToCard(homestead, 3);
		scn.AddTokensToCard(palaces, 2);

		var runner = scn.GetShadowCard("runner");
		var shadowSecond = scn.GetShadowCard("farin");
		var shadowTop = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(freepsTop);
		scn.MoveCardsToTopOfDeck(shadowSecond, shadowTop);
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, shadowSecond, shadowTop);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(freepsTop, scn.GetFreepsTopOfDeck());
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		// Both costs can be paid: the player is asked which.
		assertEquals(List.of("Discard this from play", "Remove 2 tokens from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 2 tokens from here");

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		assertInDiscard(shadowTop);
		assertEquals(shadowSecond, scn.GetShadowTopOfDeck());
		assertEquals(freepsTop, scn.GetFreepsTopOfDeck());

		// With one token left, only the discard can be paid, and it is paid unasked; the tokens on the
		// other [dwarven] condition are not "from here".
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		assertInDiscard(homestead);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertInDiscard(shadowSecond);
		assertEquals(freepsTop, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
	}
}
