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

public class Card_12_048_Tests
{

	// The Fellowship path, with Rivendell Waterfall 1_342 (Forest, Sanctuary) as site 3 and Balin's Tomb 1_343
	// (Underground only) at site 4 as it always is. Three Shadows-block sites are laid as site 4 by
	// MoveCardToAdventurePath where a test needs them; sites 7 to 9 are never reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "12_48");
					put("faramir", "6_121");    // Faramir, Ithilien Ranger: his cost text is for the starting fellowship, his fellowship ability used only on purpose
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] ranger Man who is no Faramir; a regroup ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion whose whole text is Knight
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; cheated in, so its "when you play" never fires
					put("runner2", "1_178");
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2, site 3; her text reaches bearers of [wraith] conditions only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");      // Rivendell Waterfall: Forest, Sanctuary; a ranger there raises the move limit, which no test reads
					remove("site7");
					remove("site8");
					remove("site9");
					put("woody", "11_266");     // Woody-End: Forest only; a Shadow ability used only on purpose
					put("slag", "11_258");      // Slag Mounds: Battleground only; adds a burden as a companion is killed
					put("pelennor", "12_190");  // Northern Pelennor: Battleground, Plains; a Shadow ability about lurkers
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Faramir's Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Faramir.<br>He is <b>damage +1</b>.<br>Each minion skirmishing Faramir at a battleground or forest site is roaming.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Faramir's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sword.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilFaramirIsInPlayAndThenMakesHimAloneDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(sword, faramir);

		scn.StartGame();
		assertInHand(sword, faramir);

		// A [gondor] ranger Man and Frodo, and no Faramir: the sword has no bearer.
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));

		// Faramir is the one bearer it can have, and it goes on him unasked.
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, faramir);
		assertNotAttachedTo(sword, ranger);
		assertNotAttachedTo(sword, frodo);

		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ranger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAForestEachMinionSkirmishingFaramirIsRoamingAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir, knight);
		scn.AttachCardsTo(faramir, sword);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var lemenya = scn.GetShadowCard("lemenya");
		var woody = scn.GetShadowSite("woody");

		scn.StartGame();
		assertAttachedTo(sword, faramir);
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(woody);
		scn.MoveMinionsToTable(runner1, runner2, lemenya);

		scn.SkipToAssignments();
		assertEquals(woody, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Site 4 is no lower than any of their site numbers (4, 4 and 3): none roams of himself.
		assertFalse(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner2, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, runner1}, new PhysicalCardImpl[]{knight, runner2});
		// Úlairë Lemenya is left over, and the Shadow player puts her on Faramir as well.
		scn.ShadowAssignToMinions(faramir, lemenya);
		assertTrue(scn.IsCharAssignedAgainst(faramir, runner1));
		assertTrue(scn.IsCharAssignedAgainst(faramir, lemenya));

		// The Knight's skirmish first: the Runner fighting him is not skirmishing Faramir, and Faramir's two
		// minions are assigned to him but not yet skirmishing.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, runner2));
		assertFalse(scn.HasKeyword(runner2, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));
		// The Knight's 6 beats the Runner's 5, which dies.
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, lemenya));
		// A [moria] Orc and a [wraith] Nazgûl, both skirmishing him.
		assertTrue(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));

		// 5 + 9 beat Faramir's 7 + 2; once his skirmish is over, neither is skirmishing him.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.IsCharSkirmishing(runner1));
		assertFalse(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAForestThatIsASanctuaryTheMinionSkirmishingFaramirIsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(faramir, sword);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		assertAttachedTo(sword, faramir);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));

		scn.FreepsAssignToMinions(faramir, lemenya);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, lemenya));
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundTheMinionSkirmishingFaramirIsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(faramir, sword);

		var runner1 = scn.GetShadowCard("runner1");
		var slag = scn.GetShadowSite("slag");

		scn.StartGame();
		assertAttachedTo(sword, faramir);
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(slag);
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		assertEquals(slag, scn.GetCurrentSite());
		assertFalse(scn.HasKeyword(runner1, Keyword.ROAMING));

		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));
		assertTrue(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundThatIsPlainsTheMinionSkirmishingFaramirIsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(faramir, sword);

		var runner1 = scn.GetShadowCard("runner1");
		var pelennor = scn.GetShadowSite("pelennor");

		scn.StartGame();
		assertAttachedTo(sword, faramir);
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(pelennor);
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		assertEquals(pelennor, scn.GetCurrentSite());
		assertFalse(scn.HasKeyword(runner1, Keyword.ROAMING));

		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));
		assertTrue(scn.HasKeyword(runner1, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherBattlegroundNorForestTheMinionSkirmishingFaramirIsNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(faramir, sword);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		assertAttachedTo(sword, faramir);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lemenya);

		// Balin's Tomb, underground and nothing else.
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.FreepsAssignToMinions(faramir, lemenya);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
